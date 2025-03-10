package xyz.gmitch215.socketmc.neoforge.machines;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import xyz.gmitch215.socketmc.instruction.Instruction;
import xyz.gmitch215.socketmc.instruction.InstructionId;
import xyz.gmitch215.socketmc.instruction.Machine;
import xyz.gmitch215.socketmc.neoforge.NeoForgeUtil;
import xyz.gmitch215.socketmc.neoforge.screen.NeoForgeGraphicsContext;
import xyz.gmitch215.socketmc.util.Identifier;
import xyz.gmitch215.socketmc.util.LifecycleMap;
import xyz.gmitch215.socketmc.util.NBTTag;
import xyz.gmitch215.socketmc.util.SerializableConsumer;
import xyz.gmitch215.socketmc.util.render.DrawingContext;
import xyz.gmitch215.socketmc.util.render.GraphicsContext;
import xyz.gmitch215.socketmc.util.render.ItemDisplayType;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static xyz.gmitch215.socketmc.neoforge.NeoForgeSocketMC.minecraft;

@InstructionId(Instruction.DRAW_CONTEXT)
@SuppressWarnings("unchecked")
public final class DrawContextMachine implements Machine {

    public static final DrawContextMachine MACHINE = new DrawContextMachine();

    private DrawContextMachine() {}

    public static final LifecycleMap<Consumer<GuiGraphics>> lifecycle = new LifecycleMap<>();

    public static void frameTick(GuiGraphics graphics, DeltaTracker delta) {
        lifecycle.run();
        lifecycle.forEach(c -> c.accept(graphics));
    }

    @Override
    public void onInstruction(@NotNull Instruction instruction) throws Exception {
        DrawingContext context = instruction.firstParameter(DrawingContext.class);
        long millis = instruction.lastLongParameter();
        
        lifecycle.store(graphics -> draw(graphics, context), millis);
    }

    public static void draw(GuiGraphics graphics, DrawingContext context) {
        for (Function<GraphicsContext, DrawingContext.Command> func : context) {
            DrawingContext.Command cmd = func.apply(NeoForgeGraphicsContext.INSTANCE);

            RenderType type = switch (cmd.getType()) {
                case DEFAULT -> RenderType.gui();
                case OVERLAY -> RenderType.guiOverlay();
                case TEXT_HIGHLIGHT -> RenderType.guiTextHighlight();
                case GHOST_RECIPE_OVERLAY -> RenderType.guiGhostRecipeOverlay();
            };

            List<DrawingContext.Modifier> modifiers = cmd.getModifiers();
            boolean modding = !modifiers.isEmpty();

            if (modding) {
                graphics.pose().pushPose();

                for (DrawingContext.Modifier mod : modifiers)
                    applyModifier(mod, graphics.pose());
            }
            
            switch (cmd.getId()) {
                case DrawingContext.H_LINE -> {
                    int minX = cmd.firstIntParameter();
                    int maxX = cmd.intParameter(1);
                    int y = cmd.intParameter(2);
                    int rgb = cmd.lastIntParameter();
                    
                    graphics.hLine(type, minX, maxX, y, rgb);
                }
                case DrawingContext.V_LINE -> {
                    int x = cmd.firstIntParameter();
                    int minY = cmd.intParameter(1);
                    int maxY = cmd.intParameter(2);
                    int rgb = cmd.lastIntParameter();
                    
                    graphics.vLine(type, x, minY, maxY, rgb);
                }
                case DrawingContext.ENABLE_SCISSOR -> {
                    int minX = cmd.firstIntParameter();
                    int minY = cmd.intParameter(1);
                    int maxX = cmd.intParameter(2);
                    int maxY = cmd.lastIntParameter();
                    
                    graphics.enableScissor(minX, minY, maxX, maxY);
                }
                case DrawingContext.DISABLE_SCISSOR -> graphics.disableScissor();
                case DrawingContext.FILL -> {
                    int minX = cmd.firstIntParameter();
                    int minY = cmd.intParameter(1);
                    int maxX = cmd.intParameter(2);
                    int maxY = cmd.intParameter(3);
                    int z = cmd.intParameter(4);
                    int rgb = cmd.lastIntParameter();
                    
                    graphics.fill(type, minX, minY, maxX, maxY, z, rgb);
                }
                case DrawingContext.FILL_GRADIENT -> {
                    int minX = cmd.firstIntParameter();
                    int minY = cmd.intParameter(1);
                    int maxX = cmd.intParameter(2);
                    int maxY = cmd.intParameter(3);
                    int z = cmd.intParameter(4);
                    int from = cmd.intParameter(5);
                    int to = cmd.lastIntParameter();
                    
                    graphics.fillGradient(type, minX, minY, maxX, maxY, z, from, to);
                }
                case DrawingContext.DRAW_CENTERED_STRING -> {
                    int x = cmd.firstIntParameter();
                    int y = cmd.intParameter(1);
                    String text = cmd.stringParameter(2);
                    int rgb = cmd.lastIntParameter();
                    
                    graphics.drawCenteredString(minecraft.font, NeoForgeUtil.fromJson(text), x, y, rgb);
                }
                case DrawingContext.DRAW_STRING -> {
                    int x = cmd.firstIntParameter();
                    int y = cmd.intParameter(1);
                    String text = cmd.stringParameter(2);
                    int rgb = cmd.intParameter(3);
                    boolean dropShadow = cmd.lastBooleanParameter();
                    
                    graphics.drawString(minecraft.font, NeoForgeUtil.fromJson(text), x, y, rgb, dropShadow);
                }
                case DrawingContext.DRAW_WORD_WRAP -> {
                    int x = cmd.firstIntParameter();
                    int y = cmd.intParameter(1);
                    int width = cmd.intParameter(2);
                    String text = cmd.stringParameter(3);
                    int rgb = cmd.intParameter(4);
                    
                    graphics.drawWordWrap(minecraft.font, NeoForgeUtil.fromJson(text), x, y, width, rgb);
                }
                case DrawingContext.OUTLINE -> {
                    int minX = cmd.firstIntParameter();
                    int minY = cmd.intParameter(1);
                    int maxX = cmd.intParameter(2);
                    int maxY = cmd.intParameter(3);
                    int rgb = cmd.lastIntParameter();
                    
                    graphics.renderOutline(minX, minY, maxX, maxY, rgb);
                }
                case DrawingContext.BLIT -> {
                    Identifier texture = cmd.firstParameter(Identifier.class);
                    int x = cmd.intParameter(1);
                    int y = cmd.intParameter(2);
                    float u = cmd.intParameter(3);
                    float v = cmd.intParameter(4);
                    int width = cmd.intParameter(5);
                    int height = cmd.intParameter(6);
                    int textureWidth = cmd.intParameter(7);
                    int textureHeight = cmd.lastIntParameter();
                    
                    graphics.blit(NeoForgeUtil.toMinecraft(texture), x, y, u, v, width, height, textureWidth, textureHeight);
                }
                case DrawingContext.BLIT_SPRITE -> {
                    Identifier sprite = cmd.firstParameter(Identifier.class);
                    int x = cmd.intParameter(1);
                    int y = cmd.intParameter(2);
                    int width = cmd.intParameter(3);
                    int height = cmd.intParameter(4);
                    int z = cmd.intParameter(5);

                    graphics.blitSprite(NeoForgeUtil.toMinecraft(sprite), x, y, z, width, height);
                }
                case DrawingContext.DRAW_TOOLTIP -> {
                    int x = cmd.firstIntParameter();
                    int y = cmd.intParameter(1);
                    List<Component> text = ((List<String>) cmd.lastParameter(List.class))
                            .stream()
                            .map(NeoForgeUtil::fromJson)
                            .toList();

                    graphics.renderTooltip(minecraft.font, text, Optional.empty(), x, y);
                }
                case DrawingContext.DRAW_ITEMSTACK -> {
                    NBTTag tag = cmd.firstParameter(NBTTag.class);
                    int x = cmd.intParameter(1);
                    int y = cmd.intParameter(2);
                    int guiOffset = cmd.intParameter(3);
                    float scale = cmd.floatParameter(4);
                    ItemDisplayType displayType = cmd.parameter(5, ItemDisplayType.class);
                    int combinedLight = cmd.intParameter(6);
                    int combinedOverlay = cmd.lastIntParameter();

                    ItemStack stack = NeoForgeUtil.toItem(tag);
                    ItemDisplayContext ctx = switch (displayType) {
                        case NONE -> ItemDisplayContext.NONE;
                        case THIRD_PERSON_LEFT_HAND -> ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                        case THIRD_PERSON_RIGHT_HAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                        case FIRST_PERSON_LEFT_HAND -> ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                        case FIRST_PERSON_RIGHT_HAND -> ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
                        case HEAD -> ItemDisplayContext.HEAD;
                        case GROUND -> ItemDisplayContext.GROUND;
                        case FIXED -> ItemDisplayContext.FIXED;
                        default -> ItemDisplayContext.GUI;
                    };

                    BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, minecraft.player, 0);
                    graphics.pose().pushPose();
                    graphics.pose().translate(x + (scale / 2), y + (scale / 2), 150 + (model.isGui3d() ? guiOffset : 0));
                    graphics.pose().scale(scale, -scale, scale);

                    boolean noLight = !model.usesBlockLight();
                    if (noLight)
                        Lighting.setupForFlatItems();

                    minecraft.getItemRenderer().render(stack, ctx, false, graphics.pose(), graphics.bufferSource(), combinedLight, combinedOverlay, model);
                    graphics.flush();
                    if (noLight)
                        Lighting.setupFor3DItems();

                    graphics.pose().popPose();
                }
            }

            if (modding)
                graphics.pose().popPose();
        }
    }

    public static void applyModifier(DrawingContext.Modifier mod, PoseStack pose) {
        switch (mod.getId()) {
            case DrawingContext.MODIFIER_SCALE -> {
                float x = mod.firstFloatParameter();
                float y = mod.floatParameter(1);
                float z = mod.lastFloatParameter();

                pose.scale(x, y, z);
            }
            case DrawingContext.MODIFIER_TRANSLATE -> {
                float x = mod.firstFloatParameter();
                float y = mod.floatParameter(1);
                float z = mod.lastFloatParameter();

                pose.translate(x, y, z);
            }
            case DrawingContext.MODIFIER_ROTATE -> {
                Quaternionf quaternion = mod.firstParameter(Quaternionf.class);

                pose.mulPose(quaternion);
            }
            case DrawingContext.MODIFIER_ROTATE_AROUND -> {
                Quaternionf quaternion = mod.firstParameter(Quaternionf.class);
                float x = mod.floatParameter(1);
                float y = mod.floatParameter(2);
                float z = mod.lastFloatParameter();

                pose.rotateAround(quaternion, x, y, z);
            }
            case DrawingContext.MODIFIER_APPLY_POSE -> {
                SerializableConsumer<Matrix4f> consumer = mod.firstParameter(SerializableConsumer.class);

                consumer.accept(pose.last().pose());
            }
            case DrawingContext.MODIFIER_APPLY_NORMAL -> {
                SerializableConsumer<Matrix3f> consumer = mod.firstParameter(SerializableConsumer.class);

                consumer.accept(pose.last().normal());
            }
        }
    }
}
