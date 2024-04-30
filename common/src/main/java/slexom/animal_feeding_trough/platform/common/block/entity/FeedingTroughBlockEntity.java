package slexom.animal_feeding_trough.platform.common.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import slexom.animal_feeding_trough.platform.common.AnimalFeedingTroughMod;
import slexom.animal_feeding_trough.platform.common.block.FeedingTroughBlock;
import slexom.animal_feeding_trough.platform.common.inventory.BlockEntityInventory;
import slexom.animal_feeding_trough.platform.common.screen.FeedingTroughScreenHandler;

import java.util.List;

public class FeedingTroughBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, BlockEntityInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final String NBT_STORED_EXP = "StoredExp";
    private int storedExp = 0;

    public FeedingTroughBlockEntity(BlockPos pos, BlockState state) {
        super(AnimalFeedingTroughMod.FEEDING_TROUGH_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, FeedingTroughBlockEntity blockEntity) {
        if (world.isClient()) {
            return;
        }

        int count = blockEntity.getStack(0).getCount();
        int newLevel = 0;
        if (count > 0) {
            newLevel = MathHelper.floor(blockEntity.getStack(0).getCount() / 16.0F) + 1;
            newLevel = Math.min(newLevel, 4);
        }
        int currentLevel = state.get(FeedingTroughBlock.LEVEL);
        if (currentLevel != newLevel) {
            BlockState blockState = state.with(FeedingTroughBlock.LEVEL, newLevel);
            world.setBlockState(pos, blockState, 3);
        }
    }

    private boolean playersAround(World world, BlockPos pos) {
        Box lookupArea = new Box(pos.getX() - 5, pos.getY() - 2, pos.getZ() - 5, pos.getX() + 5, pos.getY() + 2, pos.getZ() + 5);
        List<PlayerEntity> playersInArea = world.getEntitiesByClass(PlayerEntity.class, lookupArea, (e) -> true);
        return !playersInArea.isEmpty();
    }

    public void gatherExperienceOrbs(World world, BlockPos pos) {
        if (playersAround(world, pos)) {
            return;
        }

        Box lookupArea = new Box(pos.getX() - 2, pos.getY() - 2, pos.getZ() - 2, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
        List<ExperienceOrbEntity> experienceOrbEntities = world.getEntitiesByClass(ExperienceOrbEntity.class, lookupArea, (e) -> true);

        if (experienceOrbEntities.isEmpty()) {
            return;
        }

        experienceOrbEntities.forEach(orb -> {
            this.storedExp += orb.getExperienceAmount();
            orb.remove(Entity.RemovalReason.DISCARDED);
        });
    }

    public void dropStoredXp(World world, PlayerEntity playerEntity) {
        if (this.storedExp == 0) {
            return;
        }

        ExperienceOrbEntity entity = new ExperienceOrbEntity(world, playerEntity.getX(), playerEntity.getY() + 0.5F, playerEntity.getZ(), this.storedExp);
        world.spawnEntity(entity);
        this.storedExp = 0;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new FeedingTroughScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, this.inventory, registryLookup);
        this.storedExp = nbt.getInt(NBT_STORED_EXP);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, this.inventory, registryLookup);
        nbt.putInt(NBT_STORED_EXP, this.storedExp);
    }

}
