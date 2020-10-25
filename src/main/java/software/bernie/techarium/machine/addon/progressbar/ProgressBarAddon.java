package software.bernie.techarium.machine.addon.progressbar;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.tuple.Pair;
import software.bernie.techarium.client.screen.draw.IDrawable;
import software.bernie.techarium.machine.interfaces.IContainerComponentProvider;
import software.bernie.techarium.machine.interfaces.IFactory;
import software.bernie.techarium.machine.interfaces.IToolTippedAddon;
import software.bernie.techarium.machine.interfaces.IWidgetProvider;
import software.bernie.techarium.machine.screen.widget.ProgressBarWidget;
import software.bernie.techarium.tile.base.MachineMasterTile;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static software.bernie.techarium.client.screen.draw.GuiAddonTextures.DEFAULT_PROGRESS_BAR;

public class ProgressBarAddon implements INBTSerializable<CompoundNBT>, IWidgetProvider, IContainerComponentProvider, IToolTippedAddon {

    private final String name;

    private int posX;
    private int posY;

    private IDrawable drawable;
    private int sizeX;
    private int sizeY;

    private int progress;
    private int maxProgress;
    private int progressToAdd;
    private Predicate<MachineMasterTile<?>> canProgress;
    private Predicate<MachineMasterTile<?>> canReset;

    private Runnable onProgressFull;
    private Runnable onProgressTick;
    private Runnable onProgressStart;

    private MachineMasterTile<?> tile;

    private int tickingTime;

    private boolean progressUp;

    public ProgressBarAddon(MachineMasterTile<?> tile, int posX, int posY, int maxProgress, String name) {
        this.tile = tile;
        this.posX = posX;
        this.posY = posY;
        this.name = name;
        this.progress = 0;
        this.progressToAdd = 1;
        this.maxProgress = maxProgress;
        this.canProgress = (tileEntity) -> {
            return true;
        };
        this.canReset = (tileEntity) -> {
            return true;
        };
        this.onProgressFull = () -> {
        };
        this.onProgressTick = () -> {
        };
        this.onProgressStart = () -> {
        };
        this.progressUp = true;
        this.tickingTime = 1;
        this.drawable = DEFAULT_PROGRESS_BAR;
        this.sizeX = 28;
        this.sizeY = 2;
    }

    public ProgressBarAddon setOnProgressFull(Runnable onProgressFull) {
        this.onProgressFull = onProgressFull;
        return this;
    }

    public ProgressBarAddon setOnProgressStart(Runnable onProgressStart) {
        this.onProgressStart = onProgressStart;
        return this;
    }

    public ProgressBarAddon setOnProgressTick(Runnable onProgressTick) {
        this.onProgressTick = onProgressTick;
        return this;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public MachineMasterTile<?> getTile() {
        return tile;
    }

    public boolean canProgressUp() {
        return progressUp;
    }

    public Runnable getOnProgressStart() {
        return onProgressStart;
    }

    public Predicate<MachineMasterTile<?>> getCanReset() {
        return canReset;
    }

    public Predicate<MachineMasterTile<?>> getCanProgress() {
        return canProgress;
    }

    public ProgressBarAddon setCanReset(Predicate<MachineMasterTile<?>> canReset) {
        this.canReset = canReset;
        return this;
    }

    public ProgressBarAddon setCanProgress(Predicate<MachineMasterTile<?>> canProgress) {
        this.canProgress = canProgress;
        return this;
    }

    public void setDrawable(IDrawable drawable, int sizeY, int sizeX) {
        this.drawable = drawable;
        this.sizeY = sizeY;
        this.sizeX = sizeX;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public IDrawable getDrawable() {
        return drawable;
    }

    public Pair<Integer, Integer> getBackgroundSizeXY() {
        return tile.getActiveController().getBackgroundSizeXY();
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public String getName() {
        return name;
    }

    public void onTick() {
        if (tile != null) {
            if (Objects.requireNonNull(this.tile.getWorld()).getGameTime() % this.tickingTime == 0) {
                if (this.progressUp && this.progress < this.maxProgress) {
                    this.setProgress(this.progress + this.progressToAdd);
                    this.onProgressTick.run();
                } else if (!this.progressUp && this.progress > 0) {
                    this.setProgress(this.progress - this.progressToAdd);
                    this.onProgressTick.run();
                }
            }
            if (this.progressUp && this.progress >= this.maxProgress && this.canReset.test(tile)) {
                this.setProgress(0);
                this.onProgressFull.run();
            } else if (!this.progressUp && this.progress <= 0 && this.canReset.test(tile)) {
                this.setProgress(this.maxProgress);
                this.onProgressFull.run();
            }
        }
    }

    public int getTickingTime() {
        return tickingTime;
    }

    public int getProgressToAdd() {
        return progressToAdd;
    }

    public void setProgressToAdd(int progressToAdd) {
        this.progressToAdd = progressToAdd;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = new CompoundNBT();
        compound.putInt("progress", this.progress);
        compound.putInt("maxProgress", this.maxProgress);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.progress = nbt.getInt("progress");
        this.maxProgress = nbt.getInt("maxProgress");
    }

    @Override
    public List<IFactory<? extends Slot>> getContainerComponents() {
        return new ArrayList<>();
    }

    @Override
    public List<IFactory<? extends Widget>> getGuiWidgets() {
        return Lists.newArrayList(() -> {
            return new ProgressBarWidget(this, getPosX(), getPosY(), getSizeX(), getSizeY(), getName());
        });
    }

    @Override
    public void renderToolTip(Screen screen, int x, int y, int mouseX, int mouseY) {
        if (mouseX >= x + getPosX() && mouseX <= x + getPosX() + sizeX) {
            if (mouseY >= y + getPosY() && mouseY <= y + getPosY() + sizeY) {
                screen.renderTooltip(getTooltip(), mouseX, mouseY);
            }
        }
    }

    public List<String> getTooltip() {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(TextFormatting.GOLD + "Progress: " + TextFormatting.WHITE + (new DecimalFormat()).format((long) this.getProgress()) + TextFormatting.GOLD + "/" + TextFormatting.WHITE + (new DecimalFormat()).format((long) this.getMaxProgress()));
        int progress = (this.getMaxProgress() - this.getProgress()) / this.getProgressToAdd();
        if (!this.canProgressUp()) {
            progress = this.getMaxProgress() - progress;
        }

        tooltip.add(TextFormatting.GOLD + "ETA: " + TextFormatting.WHITE + (new DecimalFormat()).format(Math.ceil((double) (progress * this.getTickingTime()) / 20.0D)) + TextFormatting.DARK_AQUA + "s");
        return tooltip;
    }
}
