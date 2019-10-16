package mcjty.rftoolsutility.modules.screen.modulesclient.helper;

import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.api.screens.*;
import mcjty.rftoolsbase.api.screens.data.IModuleDataContents;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;

public class ClientScreenModuleHelper implements IModuleRenderHelper {

    @Override
    public void renderLevel(FontRenderer fontRenderer, int xoffset, int currenty, IModuleDataContents screenData, String label, boolean hidebar, boolean hidetext, boolean showpct, boolean showdiff,
                            int poscolor, int negcolor,
                            int gradient1, int gradient2, FormatStyle formatStyle) {

        renderLevel(fontRenderer, xoffset, currenty, screenData, label, hidebar, hidetext, showpct, showdiff, poscolor, negcolor,
                gradient1, gradient2, formatStyle, null);
    }

    private void renderLevel(FontRenderer fontRenderer, int xoffset, int currenty, IModuleDataContents screenData, String label, boolean hidebar, boolean hidetext, boolean showpct, boolean showdiff, int poscolor, int negcolor, int gradient1, int gradient2, FormatStyle formatStyle, ModuleRenderInfo renderInfo) {
        if (screenData == null) {
            return;
        }

        long maxContents  = screenData.getMaxContents();
        if (maxContents > 0) {
            if (!hidebar) {
                long contents = screenData.getContents();

                int width = 80 - xoffset + 7 + 40;
                long value = contents * width / maxContents;
                if (value < 0) {
                    value = 0;
                } else if (value > width) {
                    value = width;
                }
                RenderHelper.drawHorizontalGradientRect(xoffset, currenty, (int) (xoffset + value), currenty + 8, gradient1, gradient2);
            }
        }
        if (!hidetext) {
            String diffTxt = null;
            int col = poscolor;
            if (showdiff) {
                long diff = screenData.getLastPerTick();
                if (diff < 0) {
                    col = negcolor;
                    diffTxt = diff + " " + label + "/t";
                } else {
                    diffTxt = "+" + diff + " " + label + "/t";
                }
            } else if (maxContents > 0) {
                long contents = screenData.getContents();
                if (showpct) {
                    long value = contents * 100 / maxContents;
                    if (value < 0) {
                        value = 0;
                    } else if (value > 100) {
                        value = 100;
                    }
                    diffTxt = value + "%";
                } else {
                    diffTxt = format(String.valueOf(contents), formatStyle) + label;
                }
            }
            if (diffTxt != null) {
                if (ScreenConfiguration.useTruetype.get()) {
                    float r = (col >> 16 & 255) / 255.0f;
                    float g = (col >> 8 & 255) / 255.0f;
                    float b = (col & 255) / 255.0f;
                    // @todo 1.14 truetype!
                    FontRenderer renderer = Minecraft.getInstance().getFontResourceManager().getFontRenderer(new ResourceLocation(RFToolsUtility.MODID, "ubuntu"));
                    renderer.drawString(diffTxt, xoffset, currenty, col);
//                    ClientProxy.font.drawString(xoffset, 128 - currenty, diffTxt, 0.25f, 0.25f, -512f-40f, r, g, b, 1.0f);
                } else {
                    fontRenderer.drawString(diffTxt, xoffset, currenty, col);
                }
            }
        }
    }

    @Override
    public ITextRenderHelper createTextRenderHelper() {
        return new ScreenTextHelper();
    }

    @Override
    public ILevelRenderHelper createLevelRenderHelper() {
        return new ScreenLevelHelper();
    }

    @Override
    public void renderText(int x, int y, int color, @Nonnull ModuleRenderInfo renderInfo, String text) {
        if (text == null) {
            return;
        }
        if (renderInfo.truetype) {
//            float r = (color >> 16 & 255) / 255.0f;
//            float g = (color >> 8 & 255) / 255.0f;
//            float b = (color & 255) / 255.0f;
            // @todo 1.14 truetype
            FontRenderer renderer = Minecraft.getInstance().getFontResourceManager().getFontRenderer(new ResourceLocation(RFToolsUtility.MODID, "ubuntu"));
            renderer.drawString(text, x, y, color);
//            renderInfo.font.drawString(x, 128 - y, text, 0.25f, 0.25f, -512f-40f, r, g, b, 1.0f);
        } else {
            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
            fontRenderer.drawString(text, x, y, color);
        }
    }

    @Override
    public void renderTextTrimmed(int x, int y, int color, @Nonnull ModuleRenderInfo renderInfo, String text, int maxwidth) {
        if (text == null) {
            return;
        }
        if (renderInfo.truetype) {
            // @todo 1.14 truetype!
            FontRenderer renderer = Minecraft.getInstance().getFontResourceManager().getFontRenderer(new ResourceLocation(RFToolsUtility.MODID, "ubuntu"));
            String trimmed = renderer.trimStringToWidth(text, maxwidth / 4);
            renderer.drawString(trimmed, x, y, color);
//            String trimmed = renderInfo.font.trimStringToWidth(text, maxwidth);
//            float r = (color >> 16 & 255) / 255.0f;
//            float g = (color >> 8 & 255) / 255.0f;
//            float b = (color & 255) / 255.0f;
//            renderInfo.font.drawString(x, 128 - y, trimmed, 0.25f, 0.25f, -512f-40f, r, g, b, 1.0f);
        } else {
            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
            String trimmed = fontRenderer.trimStringToWidth(text, maxwidth / 4);
            fontRenderer.drawString(trimmed, x, y, color);
        }

    }

    private static DecimalFormat dfCommas = new DecimalFormat("###,###");

    @Override
    public String format(String in, FormatStyle style) {
        switch (style) {
            case MODE_FULL:
                return in;
            case MODE_COMPACT: {
                long contents = Long.parseLong(in);
                int unit = 1000;
                if (contents < unit) {
                    return in;
                }
                int exp = (int) (Math.log(contents) / Math.log(unit));
                char pre = "kMGTPE".charAt(exp-1);
                return String.format("%.1f %s", contents / Math.pow(unit, exp), pre);
            }
            case MODE_COMMAS:
                return dfCommas.format(Long.parseLong(in));
        }
        return in;
    }

}
