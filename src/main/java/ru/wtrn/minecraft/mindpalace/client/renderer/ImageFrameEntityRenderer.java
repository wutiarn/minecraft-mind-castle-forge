package ru.wtrn.minecraft.mindpalace.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import ru.wtrn.minecraft.mindpalace.client.texture.CachedTexture;
import ru.wtrn.minecraft.mindpalace.client.texture.TextureCache;
import ru.wtrn.minecraft.mindpalace.items.ImageFrame;
import ru.wtrn.minecraft.mindpalace.util.math.base.Facing;
import ru.wtrn.minecraft.mindpalace.util.math.box.AlignedBox;
import ru.wtrn.minecraft.mindpalace.util.math.box.BoxCorner;
import ru.wtrn.minecraft.mindpalace.util.math.box.BoxFace;

import static ru.wtrn.minecraft.mindpalace.config.ModClientConfigs.IMAGES_RENDER_DISTANCE;

public class ImageFrameEntityRenderer extends EntityRenderer<ImageFrame> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ImageFrameEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }


    @Override
    public boolean shouldRender(ImageFrame pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        if (!super.shouldRender(pLivingEntity, pCamera, pCamX, pCamY, pCamZ)) {
            return false;
        }
        Vec3 cameraPosition = new Vec3(pCamX, pCamY, pCamZ);
        Vec3 selfPosition = pLivingEntity.position();
        return cameraPosition.closerThan(selfPosition, IMAGES_RENDER_DISTANCE.get());
    }

    @Override
    public void render(ImageFrame entity, float pEntityYaw, float pPartialTick, PoseStack pose, MultiBufferSource pBuffer, int pPackedLight) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//        RenderSystem.setShaderColor(frame.brightness, frame.brightness, frame.brightness, frame.alpha);
        int textureId = entity.getTextureId();

        RenderSystem.bindTexture(textureId);
        RenderSystem.setShaderTexture(0, textureId);

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        AlignedBox box = entity.getBox(true);
        Facing facing = Facing.get(entity.getDirection());
        box.grow(facing.axis, 0.01F);
        BoxFace face = BoxFace.get(facing);

        pose.pushPose();

        Vec3i normal = face.facing.normal;
//        pose.translate(normal.getX(), normal.getY(), normal.getZ());

        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        Matrix4f mat = pose.last().pose();
        Matrix3f mat3f = pose.last().normal();
        for (BoxCorner corner : face.corners)
            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) ? 1 : 0, corner.isFacing(face.getTexV()) ? 1 : 0).color(-1)
                    .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
        tesselator.end();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        for (int i = face.corners.length - 1; i >= 0; i--) {
            BoxCorner corner = face.corners[i];
            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) ? 0 : 1, corner.isFacing(face.getTexV()) ? 1 : 0).color(-1)
                    .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
        tesselator.end();

        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ImageFrame pEntity) {
        return new ResourceLocation("back.png");
    }
}
