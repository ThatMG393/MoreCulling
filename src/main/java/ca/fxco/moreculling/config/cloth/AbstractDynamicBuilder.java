package ca.fxco.moreculling.config.cloth;

import ca.fxco.moreculling.api.config.ConfigAdditions;
import ca.fxco.moreculling.api.config.OptionOverride;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.impl.builders.FieldBuilder;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractDynamicBuilder<T, A extends AbstractConfigListEntry<T>, SELF extends FieldBuilder<T, A, SELF>> extends FieldBuilder<T, A, SELF> {
    @Nullable
    protected Consumer<T> saveConsumer = null;
    @Nullable
    protected BiConsumer<AbstractDynamicEntry<T>, T> changeConsumer = null;
    @NotNull
    protected Function<T, Optional<Text[]>> tooltipSupplier = (bool) -> Optional.empty();

    private T value = null;
    private boolean locked = false;
    private final String translationKey;

    protected AbstractDynamicBuilder(String translationKey) {
        this(translationKey, Text.translatable("text.cloth-config.reset_value"));
    }

    protected AbstractDynamicBuilder(String translationKey, Text resetButtonKey) {
        super(resetButtonKey, Text.translatable(translationKey));
        this.translationKey = translationKey;
    }

    /*
        Internal value getters & settings for inheritance
    */

    protected final T getValue() {
        return this.value;
    }

    protected final boolean getLocked() {
        return this.locked;
    }

    protected final void setLocked(boolean locked) {
        this.locked = locked;
    }

    /*
        Builder methods
    */

    public AbstractDynamicBuilder<T, A, ?> setModIncompatibility(boolean isLoaded, String modId) {
        if (isLoaded) {
            this.setTooltip(Text.translatable("moreculling.config.optionDisabled", modId));
            if (this.defaultValue != null && this.value != null && this.defaultValue.get() != this.value) {
                this.value = this.defaultValue.get();
            }
            this.locked = true;
            this.saveConsumer = null;
            this.changeConsumer = null;
            this.requireRestart(false);
        }
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> setModLimited(boolean isLoaded, Text limitedMessage) {
        if (isLoaded) {
            Optional<Text[]> currentTooltips = this.tooltipSupplier.apply(this.value);
            if (currentTooltips.isEmpty()) {
                this.setTooltip(limitedMessage);
            } else {
                Text[] tooltips = currentTooltips.get();
                Text[] newArray = new Text[tooltips.length + 1];
                for (int i = 0; i < tooltips.length; i++) newArray[i] = tooltips[i];
                newArray[tooltips.length] = limitedMessage;
                this.setTooltip(newArray);
            }
        }
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> setValue(T value) {
        Objects.requireNonNull(value);
        this.value = value;
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> setErrorSupplier(@Nullable Function<T, Optional<Text>> errorSupplier) {
        this.errorSupplier = errorSupplier;
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> requireRestart() {
        if (!this.locked) {
            this.requireRestart(true);
        }
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> setSaveConsumer(Consumer<T> saveConsumer) {
        if (!this.locked) {
            this.saveConsumer = saveConsumer;
        }
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> setChangeConsumer(BiConsumer<AbstractDynamicEntry<T>, T> changeConsumer) {
        if (!this.locked) {
            this.changeConsumer = changeConsumer;
        }
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> setDefaultValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> setDefaultValue(T defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> setTooltip(@Nullable Text... tooltip) {
        if (!this.locked) {
            this.tooltipSupplier = (val) -> Optional.ofNullable(tooltip);
        }
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> setTooltipSupplier(@NotNull Function<T, Optional<Text[]>> tooltipSupplier) {
        if (!this.locked) {
            this.tooltipSupplier = tooltipSupplier;
        }
        return this;
    }

    public AbstractDynamicBuilder<T, A, ?> setTooltipSupplier(@NotNull Supplier<Optional<Text[]>> tooltipSupplier) {
        if (!this.locked) {
            this.tooltipSupplier = (val) -> (Optional<Text[]>) tooltipSupplier.get();
        }
        return this;
    }

    @Override
    public final @NotNull A build() {
        Objects.requireNonNull(this.value);
        OptionOverride optionOverride = ConfigAdditions.getDisabledOptions().get(this.translationKey);
        if (optionOverride != null && !optionOverride.canChange().getAsBoolean()) {
            this.setTooltip(Text.literal(optionOverride.reason()));
            if (this.defaultValue != null && this.value != null && this.defaultValue.get() != this.value) {
                this.value = this.defaultValue.get();
            }
            this.locked = true;
            this.requireRestart(false);
        }
        return this.runBuild();
    }

    abstract public A runBuild();
}
