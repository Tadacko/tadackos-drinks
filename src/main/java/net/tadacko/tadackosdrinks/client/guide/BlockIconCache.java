package net.tadacko.tadackosdrinks.client.guide;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders a block model once into an offscreen RGBA texture, then caches the result, so callers
 * can blit the flat result as one quad with real alpha (avoiding the "see interior" artifact from
 * blending raw untested 3D geometry directly).
 *
 * HOW: calls GuideBlockScenes.renderBlockModel(...) UNCHANGED - the exact RenderType/
 * MultiBufferSource-based method already proven correct elsewhere (flowchart sub-icons,
 * BlockScene rendering). We deliberately do NOT hand-roll shader/vertex-format code (an earlier
 * attempt at that kept missing pieces RenderType normally handles automatically - depth state,
 * lightmap binding, atlas binding - and was hard to diagnose without a GPU debugger).
 *
 * The ONE thing RenderType does that fights a custom FBO is force-rebind
 * Minecraft.getMainRenderTarget() as part of its own setupRenderState(). So we temporarily swap
 * what that call RETURNS, via reflection on Minecraft's private render-target field, so that
 * forced rebind lands on our FBO instead of the real window framebuffer. Always restored in a
 * finally block.
 *
 * Field is resolved by TYPE (the one RenderTarget-typed field on Minecraft), not by a hardcoded
 * name - a hardcoded name is a plausible source of dev-vs-packaged-jar discrepancies if the
 * resolved field ever differs between environments. Every failure path below logs instead of
 * silently swallowing, so if this ever breaks again the log will say exactly where.
 */
final class BlockIconCache {
    private BlockIconCache() {}

    private static final Field MAIN_TARGET_FIELD = resolveField();

    private static Field resolveField() {
        for (Field f : Minecraft.class.getDeclaredFields()) {
            if (RenderTarget.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                return f;
            }
        }
        System.err.println("[TadackosDrinks] BlockIconCache: no RenderTarget-typed field found on Minecraft - block background icons disabled.");
        return null;
    }

    private record Key(BlockState state, int size) {}
    private static final Map<Key, RenderTarget> CACHE = new HashMap<>();

    /** Returns a GL texture id for this block's icon, or -1 if unavailable (reflection or render failed). */
    static int get(PageContext ctx, BlockState state, int size) {
        if (MAIN_TARGET_FIELD == null) return -1;
        RenderTarget cached = CACHE.computeIfAbsent(new Key(state, size), k -> render(ctx, state, size));
        return cached == null ? -1 : cached.getColorTextureId();
    }

    private static RenderTarget render(PageContext ctx, BlockState state, int size) {
        Minecraft mc = Minecraft.getInstance();

        // FBO must be sized in physical pixels, not logical GUI pixels, or the result gets
        // upscaled (blurry) when later blitted at real screen resolution. Everything else
        // on screen benefits from this scale automatically via the main render target;
        // our offscreen target needs it applied explicitly.
        Window window = mc.getWindow();
        double guiScale = window.getGuiScale();
        int texSize = Math.max(1, Mth.ceil(size * guiScale));

        RenderTarget fbo = new TextureTarget(texSize, texSize, true, Minecraft.ON_OSX); // true = needs its own depth buffer
        fbo.setClearColor(0f, 0f, 0f, 0f);
        RenderTarget realMain;
        try {
            realMain = (RenderTarget) MAIN_TARGET_FIELD.get(mc);
        } catch (IllegalAccessException e) {
            System.err.println("[TadackosDrinks] BlockIconCache: failed to read render target field: " + e);
            return null;
        }

        PoseStack modelView = null;
        try {
            MAIN_TARGET_FIELD.set(mc, fbo); // redirect getMainRenderTarget() -> fbo for this draw only

            fbo.clear(Minecraft.ON_OSX);
            fbo.bindWrite(true);

            RenderSystem.backupProjectionMatrix();
            Matrix4f ortho = new Matrix4f().setOrtho(0, size, size, 0, -1000, 1000);
            RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);

            // GameRenderer leaves its own GUI-frame model-view transform active in RenderSystem's
            // separate static matrix. Reset to identity for the duration of this isolated draw, then restore.
            modelView = RenderSystem.getModelViewStack();
            modelView.pushPose();
            modelView.setIdentity();
            RenderSystem.applyModelViewMatrix();

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f); // defensive: bakes into the cache once, don't inherit stale alpha

            // Proven path - identical call used for flowchart sub-icons / BlockScene rendering.
            // Its internal RenderType draws now rebind onto `fbo` (via the field swap above),
            // and RenderType's own state setup handles depth test/write, atlas + lightmap
            // binding for us - none of that needs to be replicated by hand here.
            GuideBlockScenes.renderBlockModel(new PoseStack(), ctx, state, 0, 0, size, 0, 0, 0);
        } catch (IllegalAccessException e) {
            System.err.println("[TadackosDrinks] BlockIconCache: failed to swap render target field: " + e);
            return null;
        } catch (Throwable t) {
            // Anything else (GL state, atlas binding, etc.) - log instead of failing silently.
            // A render exception caught further up the call stack would otherwise make the icon
            // vanish with zero trace, which is exactly the bug this class previously had.
            System.err.println("[TadackosDrinks] BlockIconCache: render failed for " + state + ": " + t);
            t.printStackTrace();
            return null;
        } finally {
            try {
                MAIN_TARGET_FIELD.set(mc, realMain); // always restore, even if the draw threw
            } catch (IllegalAccessException e) {
                System.err.println("[TadackosDrinks] BlockIconCache: failed to restore render target field: " + e);
            }
            if (modelView != null) {
                modelView.popPose();
                RenderSystem.applyModelViewMatrix();
            }
            RenderSystem.restoreProjectionMatrix();
            realMain.bindWrite(true); // rebind + restore viewport for normal GUI rendering to continue
        }
        return fbo;
    }
}