package jobieskii.anviltweaks.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

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

    // stop increasing enchantment level when at getMaxLevel()
    @Inject(
            method = "updateResult",
            at = @At(
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    value = "INVOKE"
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void inject(CallbackInfo ci, ItemStack itemStack, int i, int j, int k, ItemStack itemStack2, ItemStack itemStack3, Map map, boolean bl, Map map2, boolean bl2, boolean bl3, Iterator var12, Enchantment enchantment, int q, int r) {
        r = ((Integer) map2.get(enchantment)).intValue();
        r = q == r && r < enchantment.getMaxLevel() ? r + 1 : Math.max(r, q);
        map.put(enchantment, r);
    }
    @Redirect(
            method = "updateResult",
            at = @At(
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    value = "INVOKE"
            )
    )
    public Object ignore(Map instance, Object k, Object v) {
        return null;
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
