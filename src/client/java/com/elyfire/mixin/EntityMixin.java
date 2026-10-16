package com.elyfire.mixin;

import com.elyfire.client.VisualFireManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "doesRenderOnFire", at = @At("HEAD"), cancellable = true)
    private void elyfire$doesRenderOnFire(CallbackInfoReturnable<Boolean> cir) {
        if (VisualFireManager.isVisualFireActive((Entity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
