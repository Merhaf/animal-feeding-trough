package slexom.animal_feeding_trough.platform.common.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slexom.animal_feeding_trough.platform.common.goal.entity.ai.SelfFeedGoal;

import java.util.Arrays;
import java.util.stream.Collectors;

@Mixin(AnimalEntity.class)
public class AnimalEntityMixin extends MobEntity {
    @Unique
    private static final ItemStack[] FORBIDDEN_ITEMS = Arrays.stream(new Item[]{Items.CARROT_ON_A_STICK, Items.WARPED_FUNGUS_ON_A_STICK}).map(ItemStack::new).toArray(ItemStack[]::new);

    protected AnimalEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void AFTAddSelfFeedingGoal(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) {
        if (world != null && !world.isClient) {
            ((GoalSelectorAccessor) this.goalSelector)
                    .getGoals()
                    .stream()
                    .filter(prioritizedGoal -> prioritizedGoal.getGoal().getClass().equals(TemptGoal.class))
                    .toList()
                    .forEach(prioritizedGoal -> {
                        TemptGoal goal = (TemptGoal) prioritizedGoal.getGoal();
                        PathAwareEntity mob = ((TemptGoalAccessor) goal).getMob();
                        double speed = ((TemptGoalAccessor) goal).getSpeed();
                        Ingredient food = (((TemptGoalAccessor) goal).getFood());
                        boolean hasForbiddenFood = false;
                        for (ItemStack itemStack : FORBIDDEN_ITEMS) {
                            if (food.test(itemStack)) {
                                hasForbiddenFood = true;
                                break;
                            }
                        }
                        if (!hasForbiddenFood) {
                            this.goalSelector.add(prioritizedGoal.getPriority() + 1, new SelfFeedGoal((AnimalEntity) mob, speed, food));
                        }
                    });
        }
    }
}
