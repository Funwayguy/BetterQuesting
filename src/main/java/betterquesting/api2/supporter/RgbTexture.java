package betterquesting.api2.supporter;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;

public class RgbTexture extends AbstractTexture {
    private final int[] rgbAry;

    private final int w;
    private final int h;

    public RgbTexture(int w, int h, int[] rgbAry) {
        this.w = w;
        this.h = h;
        this.rgbAry = rgbAry;
    }

    @Override
    public void loadTexture(@Nonnull IResourceManager resourceManager) throws IOException {
        this.deleteGlTexture();

        BufferedImage bufImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final int[] imgData = ((DataBufferInt) bufImg.getRaster().getDataBuffer()).getData();
        System.arraycopy(rgbAry, 0, imgData, 0, imgData.length);
        TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), bufImg, false, false);
    }
}
