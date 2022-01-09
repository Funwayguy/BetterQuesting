package betterquesting.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api2.client.gui.controls.io.ValueFuncIO;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelEntityPreview;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.core.BetterQuesting;
import betterquesting.client.themes.BQSTextures;
import betterquesting.questing.tasks.TaskInteractEntity;
import mezz.jei.Internal;
import mezz.jei.api.recipe.IFocus.Mode;
import mezz.jei.gui.Focus;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional.Method;

import java.util.UUID;

public class PanelTaskInteractEntity extends CanvasEmpty
{
    private final TaskInteractEntity task;
    
    public PanelTaskInteractEntity(IGuiRect rect, TaskInteractEntity task)
    {
        super(rect);
        this.task = task;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        PanelItemSlot itemSlot = new PanelItemSlot(new GuiTransform(GuiAlign.MID_LEFT, 0, -48, 32, 32, 0), -1, task.targetItem, false, true);
        if(BetterQuesting.hasJEI) itemSlot.setCallback(value -> lookupRecipe(value.getBaseStack()));
        this.addPanel(itemSlot);
        
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_LEFT, 32, -40, 16, 16, 0), PresetIcon.ICON_RIGHT.getTexture()));
        
        UUID playerID = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
        int prog = task.getUsersProgress(playerID);
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.MID_LEFT, 0, -14, 32, 14, 0), prog + "/" + task.required).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));
        
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_LEFT, 0, 0, 24, 24, 0), BQSTextures.HAND_LEFT.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_LEFT, 24, 0, 24, 24, 0), BQSTextures.HAND_RIGHT.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_LEFT, 0, 24, 24, 24, 0), BQSTextures.ATK_SYMB.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_LEFT, 24, 24, 24, 24, 0), BQSTextures.USE_SYMB.getTexture()));
        
        IGuiTexture txTick = new GuiTextureColored(PresetIcon.ICON_TICK.getTexture(), new GuiColorStatic(0xFF00FF00));
        IGuiTexture txCross = new GuiTextureColored(PresetIcon.ICON_CROSS.getTexture(), new GuiColorStatic(0xFFFF0000));
        
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_LEFT, 16, 16, 8, 8, 0), task.useOffHand ? txTick : txCross));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_LEFT, 40, 16, 8, 8, 0), task.useMainHand ? txTick : txCross));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_LEFT, 16, 40, 8, 8, 0), task.onHit ? txTick : txCross));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_LEFT, 40, 40, 8, 8, 0), task.onInteract ? txTick : txCross));
        
        
        ResourceLocation targetRes = new ResourceLocation(task.entityID);
        Entity target;
        
        if(EntityList.isRegistered(targetRes))
        {
            target = EntityList.createEntityByIDFromName(targetRes, Minecraft.getMinecraft().world);
            if(target != null) target.readFromNBT(task.entityTags);
        } else
        {
            target = null;
        }
        
        if(target != null) this.addPanel(new PanelEntityPreview(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(48, 0, 0, 0), 0), target).setRotationDriven(new ValueFuncIO<>(() -> 15F), new ValueFuncIO<>(() -> (float)(Minecraft.getSystemTime()%30000L / 30000D * 360D))));
    }
    
    @Method(modid = "jei")
    private void lookupRecipe(ItemStack stack)
    {
        if(stack == null || stack.isEmpty() || Internal.getRuntime() == null) return;
        Internal.getRuntime().getRecipesGui().show(new Focus<>(Mode.OUTPUT, stack));
    }
}
