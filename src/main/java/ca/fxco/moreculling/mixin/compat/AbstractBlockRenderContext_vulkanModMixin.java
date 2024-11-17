package ca.fxco.moreculling.mixin.compat;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.vulkanmod.render.chunk.build.frapi.render.AbstractBlockRenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import ca.fxco.moreculling.MoreCulling;
import ca.fxco.moreculling.utils.CullingUtils;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;

@Restriction(require = @Condition(value = "vulkanmod", versionPredicates = { ">=0.5.0-build.1" }))
@Mixin(AbstractBlockRenderContext.class)
public class AbstractBlockRenderContext_vulkanModMixin {
    @Shadow
    protected BlockRenderView renderRegion;

    @Shadow
    protected BlockPos blockPos;

    @Inject(
        method = "faceNotOccluded",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/BlockView;getBlockState(" +
                     "Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
            shift = At.Shift.BEFORE
        ),
        cancellable = true                                                                                                                                           
    )
    private void moreCulling$useMoreCulling(
        BlockState blockState,
        Direction face,
        CallbackInfoReturnable<Boolean> cir,
        @Local BlockPos adjPos
    ) {
        if (MoreCulling.CONFIG.useBlockStateCulling) {
            cir.setReturnValue(
                CullingUtils.shouldDrawSideCulling(
                    blockState, renderRegion, blockPos, face, adjPos
                )
            );
        }
    }
}
