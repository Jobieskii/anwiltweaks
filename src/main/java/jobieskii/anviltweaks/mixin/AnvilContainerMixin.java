package jobieskii.anviltweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public class AnvilContainerMixin {

    // mixin appropriated from OnyxStudios / AnvilFix
    // Change hardcoded limit for experience levels at the anvil
    @ModifyConstant(method= "updateResult", constant = @Constant(intValue = 40,ordinal = 2))
    public int maxLevel(int i) {
        return Integer.MAX_VALUE;
    }

    // change hardcoded cost of repairment in material (from 1/4 hp per, to 1/2 hp per)
    @ModifyConstant(
            method = "updateResult",
            constant = @Constant(intValue = 4),
            slice = @Slice(
                    from = @At(
                            target = "Lnet/minecraft/item/Item;canRepair(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
                            value = "INVOKE_ASSIGN"
                    ),
                    to = @At(
                            target = "Lnet/minecraft/item/ItemStack;getMaxDamage()I",
                            ordinal = 1,
                            value = "INVOKE",
                            shift = At.Shift.BY,
                            by = 2
                    )

    ))
    public int costOfRepairment(int old) {
        return 2;
    }

    // Allow increasing enchantment level above maxLevel
    @Redirect(
            method = "updateResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;getMaxLevel()I", ordinal = 0)
    )
    public int maxLevel(Enchantment instance) {
        if (instance.getMaxLevel() == 1)
            return 1;
        else
            return Integer.MAX_VALUE;
    }
    // Make cost of enchantment exponential based on level
    @Inject(
            method = "updateResult",
            at = @At(target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", value = "INVOKE", shift = At.Shift.AFTER)
    )
    public void updateR(CallbackInfo ci, @Local(ordinal=4) LocalIntRef r, @Local Enchantment enchantment) {
        int k = r.get() - enchantment.getMaxLevel();
        if (k > 0) {
            r.set(37 + (6 * k));
        }
    }
    // stop increasing repair cost
    @Inject(
            method = "getNextCost",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void repaircost(int cost, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(cost, 1));
    }
}
