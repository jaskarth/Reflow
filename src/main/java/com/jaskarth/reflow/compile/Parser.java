package com.jaskarth.reflow.compile;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.hash.NodeCache;
import com.jaskarth.reflow.optgraph.nodes.*;
import com.jaskarth.reflow.optgraph.nodes.cache.*;
import com.jaskarth.reflow.optgraph.nodes.ctrl.*;
import com.jaskarth.reflow.optgraph.nodes.vars.Arg1Node;
import com.jaskarth.reflow.optgraph.nodes.vars.CtxArgNode;
import com.jaskarth.reflow.runtime.resource.*;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Parser {
    public static CompileUnit parseFully(SharedCompileData shared, DensityFunction func) {
        ParseContext ctx = new ParseContext(shared);

        // Setup start
        StartNode start = new StartNode();
        ctx.ctrl = ctx.get(start);
        ctx.start = ctx.ctrl;

        Node body = ctx.get(parse(ctx, func));

        ReturnNode ret = new ReturnNode(body);
        ret.addIn(0, ctx.ctrl);
        return new CompileUnit(ctx.cache, ctx.resources, ctx.get(ret), (StartNode) ctx.start);
    }

    private static Node parse(ParseContext ctx, DensityFunction func) {
        if (func instanceof DensityFunctions.Mapped(DensityFunctions.Mapped.Type type, DensityFunction input, double u1, double u2)) {
            return new UnaryNode(type, ctx.get(parse(ctx, input)));
        } else if (func instanceof DensityFunctions.Constant(double v)) {
            return new ImmDNode(v);
        } else if (func instanceof DensityFunctions.TwoArgumentSimpleFunction binary) {
            return new BinaryNode(binary.type(), ctx.get(parse(ctx, binary.argument1())), ctx.get(parse(ctx, binary.argument2())));
        } else if (func instanceof DensityFunctions.HolderHolder(Holder<DensityFunction> function)) {
            return parse(ctx, function.value());
        } else if (func instanceof DensityFunctions.BlendDensity(DensityFunction function)) {
            return parse(ctx, function);
        } else if (func instanceof DensityFunctions.RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange)) {
            return new UnimplementedNode("RangeChoice(" + minInclusive + " - " + maxExclusive + ")", ctx.get(parse(ctx, input)), ctx.get(parse(ctx, whenInRange)), ctx.get(parse(ctx, whenOutOfRange)));
        } else if (func instanceof DensityFunctions.FindTopSurface surface) {
            return new UnimplementedNode("FindTop", ctx.get(parse(ctx, surface.density())), ctx.get(parse(ctx, surface.upperBound())));
        } else if (func instanceof DensityFunctions.YClampedGradient(int fromY, int toY, double fromValue, double toValue)) {
            Node y = ctx.get(new ConvI2DNode(ctx.get(new CtxArgNode(CtxArgNode.Arg.Y))));
            Node fy = ctx.get(new ImmDNode(fromY));
            Node ty = ctx.get(new ImmDNode(toY));
            Node fv = ctx.get(new ImmDNode(fromValue));
            Node tv = ctx.get(new ImmDNode(toValue));

            return new ClampedLerpNode(fv, tv, ctx.get(new InverseLerpNode(y, fy, ty)));
        } else if (func instanceof DensityFunctions.Clamp(DensityFunction input, double minValue, double maxValue)) {
            return new UnimplementedNode("Clamp(" + minValue + "," + maxValue + ")", ctx.get(parse(ctx, input)));
        } else if (func instanceof DensityFunctions.Marker(DensityFunctions.Marker.Type type, DensityFunction wrapped)) {
            // TODO: this setup doesn't work! all cache nodes need a getfield node input, otherwise the stack setup doesn't work

            if (type == DensityFunctions.Marker.Type.Cache2D) {
                // Find resource
                ResourceLease lease = ctx.resources.request(ResourceTypes.CACHE_2D);

                Node resource = ctx.get(new LoadResourceNode(lease));
                Node pos2d = ctx.get(new Pos2dNode(ctx.get(new CtxArgNode(CtxArgNode.Arg.X)), ctx.get(new CtxArgNode(CtxArgNode.Arg.Z))));

                // Generate control flow
                Node ifn = ctx.get(new IfNode(ctx.ctrl, ctx.get(new Cache2DCheckNode(resource, pos2d))));
                Node ifTrue = ctx.get(new IfProjNode(ifn, IfProjNode.IfType.TRUE));
                Node ifFalse = ctx.get(new IfProjNode(ifn, IfProjNode.IfType.FALSE));
                
                ctx.ctrl = ifFalse;
                Node body = ctx.get(parse(ctx, wrapped));

                Node reg = ctx.get(new RegionNode(ifTrue, ctx.ctrl));
                Node phi = new PhiNode(reg, ctx.get(new Cache2DGetNode(resource)),
                        ctx.get(new Cache2DGetSetNode(resource, pos2d, body)));
                ctx.ctrl = reg;

                return phi;
            }

            if (type == DensityFunctions.Marker.Type.FlatCache) {
                // Find resource
                CacheFlat flat = ctx.shared.getOrMake(func);
//                ResourceLease lease = ctx.resources.request(ResourceTypes.flat(flat));
                ResourceLease lease = ctx.resources.request(ResourceTypes.flatWrapper(new CacheFlatWrapper(func, flat)));

                Node resource = ctx.get(new LoadResourceNode(lease));

                // Generate control flow
                Node pos2d = ctx.get(new Pos2dNode(ctx.get(new CtxArgNode(CtxArgNode.Arg.X)), ctx.get(new CtxArgNode(CtxArgNode.Arg.Z))));
                Node ifn = ctx.get(new IfNode(ctx.ctrl, ctx.get(new FlatCacheCheckNode(resource, pos2d))));
                Node ifTrue = ctx.get(new IfProjNode(ifn, IfProjNode.IfType.TRUE));
                Node ifFalse = ctx.get(new IfProjNode(ifn, IfProjNode.IfType.FALSE));

                ctx.ctrl = ifFalse;
                // Fold FlatCache(Cache2d(...)) to FlatCache(...)
                if (wrapped instanceof DensityFunctions.HolderHolder holder) {
                    wrapped = holder.function().value();
                }
                if (wrapped instanceof DensityFunctions.Marker(DensityFunctions.Marker.Type type1, DensityFunction wrapped1)
                        && type1 == DensityFunctions.Marker.Type.Cache2D) {
                    wrapped = wrapped1;
                }
                Node body = ctx.get(parse(ctx, wrapped));

                Node reg = ctx.get(new RegionNode(ifTrue, ctx.ctrl));
                Node phi = new PhiNode(reg, ctx.get(new FlatCacheGetNode(resource, pos2d)),
                        ctx.get(new FlatCacheGetSetNode(resource, pos2d, body)));
                ctx.ctrl = reg;

                return phi;
            }

            if (type == DensityFunctions.Marker.Type.CacheOnce) {
                // Find resource
                ResourceLease lease = ctx.resources.request(ResourceTypes.CACHE_ONCE);

                Node resource = ctx.get(new LoadResourceNode(lease));

                // Generate control flow
                Node ifn = ctx.get(new IfNode(ctx.ctrl, ctx.get(new OnceCacheCheckNode(resource, ctx.get(new Arg1Node())))));
                Node ifTrue = ctx.get(new IfProjNode(ifn, IfProjNode.IfType.TRUE));
                Node ifFalse = ctx.get(new IfProjNode(ifn, IfProjNode.IfType.FALSE));

                ctx.ctrl = ifFalse;
                Node body = ctx.get(parse(ctx, wrapped));

                Node reg = ctx.get(new RegionNode(ifTrue, ctx.ctrl));
                Node phi = new PhiNode(reg, ctx.get(new OnceCacheGetNode(resource)),
                        ctx.get(new OnceCacheGetSetNode(resource, ctx.get(new Arg1Node()), body)));
                ctx.ctrl = reg;

                return phi;
            }

            return new MarkerNode(type, ctx.get(parse(ctx, wrapped)));
        } else if (func instanceof DensityFunctions.Spline(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> spline)) {
            List<Node> c = new ArrayList<>();
            AtomicInteger i = new AtomicInteger();
            spline.mapAll(v -> {
//                System.out.println(parse(v.function().value()).describe());
//                DotExporter.export("Spline_" + i.getAndIncrement(), parse(ctx, v.function().value()));
//                c.add(parse(ctx, v.function().value()));
                return v;
            });
//            return new UnimplementedNode("Spline", c.toArray(Node[]::new));
            ResourceLease lease = ctx.resources.request(ResourceTypes.spline(spline));
            Node load = ctx.get(new LoadResourceNode(lease));

            return new SplineNode(load, ctx.get(new Arg1Node()));
        } else if (func instanceof DensityFunction.SimpleFunction simple) {
            if (simple instanceof DensityFunctions.BlendOffset) {
                return new BlendOffsetNode();
            }
            if (simple instanceof DensityFunctions.BlendAlpha) {
                return new BlendAlphaNode();
            }
            if (simple instanceof DensityFunctions.EndIslandDensityFunction) {
                ResourceLease lease = ctx.resources.request(ResourceTypes.arbitrary(simple));
                Node load = ctx.get(new LoadResourceNode(lease));
                return new ArbitraryNode("EndIslandDensity", load, ctx.get(new Arg1Node()));
            }

            return new UnimplementedNode("Simple/" + simple.getClass().getSimpleName());
        } else if (func instanceof DensityFunctions.ShiftedNoise(
                DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale,
                DensityFunction.NoiseHolder noise
        )) {
            ResourceLease lease = ctx.resources.request(ResourceTypes.noise(noise.noise()));
            Node load = ctx.get(new LoadResourceNode(lease));

            Node x = ctx.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL,
                    ctx.get(new ConvI2DNode(ctx.get(new CtxArgNode(CtxArgNode.Arg.X)))), ctx.get(new ImmDNode(xzScale))));
            Node y = ctx.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL,
                    ctx.get(new ConvI2DNode(ctx.get(new CtxArgNode(CtxArgNode.Arg.Y)))), ctx.get(new ImmDNode(yScale))));
            Node z = ctx.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL,
                    ctx.get(new ConvI2DNode(ctx.get(new CtxArgNode(CtxArgNode.Arg.Z)))), ctx.get(new ImmDNode(xzScale))));

            x = ctx.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.ADD, ctx.get(parse(ctx, shiftX)), x));
            y = ctx.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.ADD, ctx.get(parse(ctx, shiftY)), y));
            z = ctx.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.ADD, ctx.get(parse(ctx, shiftZ)), z));

            return new NoiseNode(getNoiseId(noise).orElseThrow(), load, x, y, z);
        } else if (func instanceof DensityFunctions.ShiftA(DensityFunction.NoiseHolder noise)) {
            ResourceLease lease = ctx.resources.request(ResourceTypes.noise(noise.noise()));
            Node load = ctx.get(new LoadResourceNode(lease));

            Node _025 = ctx.get(new ImmDNode(0.25));
            Node _4 = ctx.get(new ImmDNode(4));
            Node x = ctx.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL,
                    ctx.get(new ConvI2DNode(ctx.get(new CtxArgNode(CtxArgNode.Arg.X)))), _025));
            Node y = ctx.get(new ImmDNode(0));
            Node z = ctx.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL,
                    ctx.get(new ConvI2DNode(ctx.get(new CtxArgNode(CtxArgNode.Arg.Z)))), _025));

            return new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL,
                    ctx.get(new NoiseNode(getNoiseId(noise).orElseThrow(), load, x, y, z)), _4);
        } else if (func instanceof DensityFunctions.ShiftB(DensityFunction.NoiseHolder noise)) {
            ResourceLease lease = ctx.resources.request(ResourceTypes.noise(noise.noise()));
            Node load = ctx.get(new LoadResourceNode(lease));

            Node _025 = ctx.get(new ImmDNode(0.25));
            Node _4 = ctx.get(new ImmDNode(4));
            // Watch it! The xyz order is important!
            Node x = ctx.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL,
                    ctx.get(new ConvI2DNode(ctx.get(new CtxArgNode(CtxArgNode.Arg.Z)))), _025));
            Node y = ctx.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL,
                    ctx.get(new ConvI2DNode(ctx.get(new CtxArgNode(CtxArgNode.Arg.X)))), _025));
            Node z = ctx.get(new ImmDNode(0));

            return new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL,
                    ctx.get(new NoiseNode(getNoiseId(noise).orElseThrow(), load, x, y, z)), _4);
        } else if (func instanceof DensityFunctions.MarkerOrMarked markerImpl) {
            return new UnimplementedNode("MarkerImpl/" + markerImpl, ctx.get(parse(ctx, markerImpl.wrapped())));
        } else if (func instanceof DensityFunctions.Noise(DensityFunction.NoiseHolder noise, double xzScale, double yScale)) {
            return new NoiseNode2(NoiseNode2.Type.NoShift, getNoiseId(noise).orElseThrow(), xzScale, yScale);
        } else if (func instanceof DensityFunctions.WeirdScaledSampler(
                DensityFunction input, DensityFunction.NoiseHolder noise, DensityFunctions.WeirdScaledSampler.RarityValueMapper mapper
        ) ) {
            return new UnimplementedNode("WeirdScaled", ctx.get(parse(ctx, input)));
        }

        return new Node() {
            @Override
            public int req() {
                return 0;
            }

            @Override
            public String describe() {
                return "" + func.getClass();
            }
        };
    }

    private static class ParseContext {
        private final NodeCache cache = new NodeCache();
        private final ResourceTracker resources = new ResourceTracker();
        private final SharedCompileData shared;
        private Node ctrl;
        private Node start;

        public ParseContext(SharedCompileData shared) {
            this.shared = shared;
        }

        private Node get(Node in) {
            Node node = cache.get(in);
            cache.checkHashes();
            return node;
//            return in;
        }
    }

    private static String stringify(DensityFunction.NoiseHolder noise) {
        return getNoiseId(noise).orElse("unbound!!");
    }

    private static @NotNull Optional<String> getNoiseId(DensityFunction.NoiseHolder noise) {
        return noise.noiseData().unwrapKey().map(ResourceKey::location).map(ResourceLocation::toString);
    }
}
