package slexom.animal_feeding_trough.platform.common.goal.entity.ai;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import slexom.animal_feeding_trough.platform.common.block.entity.FeedingTroughBlockEntity;

import java.util.function.Predicate;

public class SelfFeedGoal extends MoveToTargetPosGoal {

    protected final AnimalEntity mob;
    private final Predicate<ItemStack> foodPredicate;

    private FeedingTroughBlockEntity feeder;

    public SelfFeedGoal(AnimalEntity mob, double speed, Predicate<ItemStack> foodPredicate) {
        super(mob, speed, 8);
        this.mob = mob;
        this.foodPredicate = foodPredicate;
    }

    @Override
    public boolean canStart() {
        return this.mob.canEat() && this.mob.getBreedingAge() == 0 && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && this.feeder != null && this.mob.canEat() && this.mob.getBreedingAge() == 0;
    }

    @Override
    public double getDesiredDistanceToTarget() {
        return 2.0D;
    }

    private boolean hasCorrectFood(ItemStack itemStack) {
        return this.foodPredicate.test(itemStack);
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FeedingTroughBlockEntity feedingTroughBlockEntity) {
            ItemStack itemStack = feedingTroughBlockEntity.getItems().get(0);
            if (!itemStack.isEmpty() && hasCorrectFood(itemStack)) {
                this.feeder = feedingTroughBlockEntity;
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        World world = this.mob.getWorld();
        if (!world.isClient && this.feeder != null && this.mob.canEat()) {
            if (!this.feeder.getItems().get(0).isEmpty()) {
                this.mob.getLookControl().lookAt((double) this.targetPos.getX() + 0.5D, this.targetPos.getY(), (double) this.targetPos.getZ() + 0.5D, 10.0F, (float) this.mob.getMaxLookPitchChange());
                if (this.hasReached()) {
                    this.feeder.getItems().get(0).decrement(1);
                    this.mob.lovePlayer(null);
                }
            }
            this.feeder = null;
        }
        super.tick();
    }

}