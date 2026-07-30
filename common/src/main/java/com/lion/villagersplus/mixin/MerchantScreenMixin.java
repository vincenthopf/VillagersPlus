package com.lion.villagersplus.mixin;

import com.lion.villagersplus.VillagersPlus;
import com.lion.villagersplus.client.TradeCatalogClientState;
import com.lion.villagersplus.client.screen.TradeCatalogPanel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds the config-gated trade controls (set level, re-roll, catalog) to the vanilla trade screen,
 * and hosts the catalogue panel docked against its left edge.
 *
 * <p>Control buttons send their action through the vanilla ButtonClick packet (see
 * {@link MerchantScreenHandlerMixin}); only the catalogue's <em>reply</em> needs a custom packet,
 * because it carries per-trade metadata no vanilla screen sync can express.
 *
 * <p>Extends the real superclass {@link HandledScreen} so inherited members ({@code x}, {@code y},
 * {@code backgroundWidth}, {@code width}, {@code addDrawableChild}, {@code getScreenHandler}) are
 * used directly without shadows.
 */
@Environment(EnvType.CLIENT)
@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> {

    private MerchantScreenMixin() {
        super(null, null, null); // never executed; present only to satisfy the compiler
    }

    /**
     * Vanilla's trade-list scrollbar drag flag. {@code MerchantScreen.mouseClicked} is the only
     * place it is ever cleared, so cancelling that method would strand a drag in progress.
     */
    @Shadow
    private boolean scrolling;

    @Unique private int villagersplus$level = 1;
    @Unique private ButtonWidget villagersplus$levelDisplay;
    @Unique private ButtonWidget villagersplus$setLevelButton;
    @Unique private ButtonWidget villagersplus$rerollLevelButton;
    @Unique private ButtonWidget villagersplus$catalogButton;
    @Unique private TradeCatalogPanel villagersplus$catalog;
    @Unique private boolean villagersplus$shortLabels;

    @Inject(method = "init", at = @At("TAIL"))
    private void villagersplus$addControls(CallbackInfo ci) {
        boolean allowReroll = VillagersPlus.CONFIG.allow_trade_reroll;
        boolean allowSetLevel = VillagersPlus.CONFIG.allow_set_villager_level;
        boolean allowView = VillagersPlus.CONFIG.allow_view_all_trades;
        if (!allowReroll && !allowSetLevel && !allowView) {
            return;
        }

        if (allowView && this.villagersplus$catalog == null) {
            this.villagersplus$catalog = new TradeCatalogPanel(MinecraftClient.getInstance());
            // First init of this screen: drop whatever the last merchant sent. Guarded on the panel
            // being absent because init also re-runs on window resize, which must keep the data.
            TradeCatalogClientState.clear();
        }

        int step = 23;
        int gap = 5;
        int margin = 2;
        int maxWidth = 110;
        int minWidth = 68;

        // The control column takes the right gutter and the catalogue takes the left, so the two
        // never compete. The column is width-adaptive because the gutter is not: MerchantScreen is
        // 276 units wide and centred, so at 1920x1080 with GUI scale 4 the screen is only 480 units
        // across and each gutter is 102 — a fixed 110-wide column does not fit. It used to fall back
        // to stacking below the panel and then get clamped straight back up on top of the trades.
        int gutter = this.width - (this.x + this.backgroundWidth) - gap - margin;
        int rows = 1 + (allowSetLevel ? 1 : 0) + (allowReroll ? 2 : 0) + (allowView ? 1 : 0);

        int w;
        int baseX;
        int row;
        if (gutter >= minWidth) {
            w = Math.min(maxWidth, gutter);
            baseX = this.x + this.backgroundWidth + gap;
            row = this.y;
        } else {
            // Only reachable on a window narrower than any standard resolution produces. Sit below
            // the trade panel and, if that runs off the bottom, ride the bottom edge — overlapping
            // the hotbar is far less destructive than overlapping the trades.
            w = maxWidth;
            baseX = Math.max(margin, Math.min(this.x, this.width - w - margin));
            row = Math.min(this.y + this.backgroundHeight + 4, this.height - rows * step - margin);
        }
        row = Math.max(margin, row);

        // "Re-roll level 1" is about 74px wide, so below this the full labels clip and the short
        // variants are used instead.
        this.villagersplus$shortLabels = w < 90;

        // Shared level selector (used by set-level, per-level reroll and catalog). The arrows shrink
        // alongside the labels so the readout between them keeps a usable width.
        int arrowWidth = villagersplus$shortLabels ? 16 : 20;
        villagersplus$levelDisplay = addDrawableChild(ButtonWidget.builder(villagersplus$levelText(), b -> {})
                .dimensions(baseX + arrowWidth + 4, row, w - 2 * arrowWidth - 8, 20).build());
        villagersplus$levelDisplay.active = false;
        addDrawableChild(ButtonWidget.builder(Text.literal("<"),
                b -> villagersplus$changeLevel(-1)).dimensions(baseX, row, arrowWidth, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">"),
                b -> villagersplus$changeLevel(1)).dimensions(baseX + w - arrowWidth, row, arrowWidth, 20).build());
        row += step;

        if (allowSetLevel) {
            villagersplus$setLevelButton = addDrawableChild(ButtonWidget.builder(
                            villagersplus$label("set_level"),
                            b -> villagersplus$click(100 + villagersplus$level))
                    .dimensions(baseX, row, w, 20).build());
            row += step;
        }

        if (allowReroll) {
            villagersplus$rerollLevelButton = addDrawableChild(ButtonWidget.builder(
                            villagersplus$label("reroll_level"),
                            b -> villagersplus$click(110 + villagersplus$level))
                    .dimensions(baseX, row, w, 20).build());
            row += step;
            addDrawableChild(ButtonWidget.builder(
                            villagersplus$shortLabels
                                    ? Text.translatable("gui.villagersplus.reroll_all_short")
                                    : Text.translatable("gui.villagersplus.reroll_all"),
                            b -> villagersplus$click(120))
                    .dimensions(baseX, row, w, 20).build());
            row += step;
        }

        if (allowView) {
            villagersplus$catalogButton = addDrawableChild(ButtonWidget.builder(
                            villagersplus$label("catalog_button"),
                            b -> villagersplus$toggleCatalog())
                    .dimensions(baseX, row, w, 20).build());
        }
    }

    /** Picks the full or short label variant for a level-parameterised button. */
    @Unique
    private Text villagersplus$label(String key) {
        return Text.translatable("gui.villagersplus." + key + (villagersplus$shortLabels ? "_short" : ""),
                villagersplus$level);
    }

    /**
     * Drawn at TAIL so it lands after vanilla's own {@code drawMouseoverTooltip} and after
     * {@code Screen.render} has drawn the control buttons — the panel is entirely hand-drawn, so
     * there are no widgets of ours underneath for it to paint over.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void villagersplus$renderCatalog(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.villagersplus$catalog == null) {
            return;
        }
        villagersplus$layoutCatalog();
        this.villagersplus$catalog.render(context, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void villagersplus$catalogClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.villagersplus$catalog == null) {
            return;
        }
        villagersplus$layoutCatalog();
        if (!this.villagersplus$catalog.isOver(mouseX, mouseY)) {
            return;
        }

        // Vanilla would treat this as a click outside the GUI and throw the cursor stack on the
        // ground; cancel both halves of the click instead.
        this.scrolling = false;
        ((HandledScreenAccessor) this).villagersplus$setCancelNextRelease(true);

        int id = this.villagersplus$catalog.mouseClicked(mouseX, mouseY);
        if (id >= 0) {
            villagersplus$click(id);
        }
        cir.setReturnValue(true);
    }

    /**
     * Vanilla's {@code mouseScrolled} scrolls the trade list from anywhere on screen — it does no
     * bounds check and never calls {@code super} — so the panel has to claim the wheel explicitly.
     */
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void villagersplus$catalogScroll(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.villagersplus$catalog == null) {
            return;
        }
        villagersplus$layoutCatalog();
        if (this.villagersplus$catalog.isOver(mouseX, mouseY)) {
            this.villagersplus$catalog.mouseScrolled(amount);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void villagersplus$layoutCatalog() {
        this.villagersplus$catalog.layout(this.x, this.y, this.width, this.height);
    }

    @Unique
    private void villagersplus$toggleCatalog() {
        int id = this.villagersplus$catalog.toggle(this.villagersplus$level);
        if (id >= 0) {
            villagersplus$click(id);
        }
    }

    @Unique
    private void villagersplus$changeLevel(int delta) {
        villagersplus$level = MathHelper.clamp(villagersplus$level + delta, 1, 5);
        if (villagersplus$levelDisplay != null) {
            villagersplus$levelDisplay.setMessage(villagersplus$levelText());
        }
        if (villagersplus$setLevelButton != null) {
            villagersplus$setLevelButton.setMessage(villagersplus$label("set_level"));
        }
        if (villagersplus$rerollLevelButton != null) {
            villagersplus$rerollLevelButton.setMessage(villagersplus$label("reroll_level"));
        }
        if (villagersplus$catalogButton != null) {
            villagersplus$catalogButton.setMessage(villagersplus$label("catalog_button"));
        }
    }

    @Unique
    private Text villagersplus$levelText() {
        return villagersplus$label("level");
    }

    @Unique
    private void villagersplus$click(int id) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager != null) {
            client.interactionManager.clickButton(this.getScreenHandler().syncId, id);
        }
    }
}
