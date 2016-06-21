package cofh.thermalfoundation.fluid;

import cofh.core.fluid.BlockFluidInteractive;
import cofh.lib.util.BlockWrapper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalfoundation.ThermalFoundation;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockFluidPyrotheum extends BlockFluidInteractive {

	public static final int LEVELS = 5;
	public static final Material materialFluidPyrotheum = new MaterialLiquid(MapColor.tntColor);

	private static boolean effect = true;
	private static boolean enableSourceFall = true;

	public BlockFluidPyrotheum(Fluid fluid) {

		super(fluid, Material.lava, "thermalfoundation", "pyrotheum");
		setQuantaPerBlock(LEVELS);
		setTickRate(10);

		setHardness(1000F);
		setLightOpacity(1);
		setParticleColor(1.0F, 0.7F, 0.15F);
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {

		return effect ? 600 : 0;
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {

		return 0;
	}

	@Override
	public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {

		return effect;
	}

	@Override
	public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {

		return effect && face.ordinal() > EnumFacing.UP.ordinal() && world.getBlockState(pos.down()).getBlock() != this;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		if (!effect) {
			return;
		}
		if (ServerHelper.isClientWorld(world)) {
			return;
		}
		if (entity instanceof EntityPlayer) {

		} else if (entity instanceof EntityCreeper) {
			world.createExplosion(entity, entity.posX, entity.posY, entity.posZ, 6.0F, entity.worldObj.getGameRules().getBoolean("mobGriefing"));
			entity.setDead();
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if (effect) {
			checkForInteraction(world, pos, state);
		}
		if (enableSourceFall && getMetaFromState(state) == 0) {
			IBlockState stateDown = world.getBlockState(pos.add(0, densityDir, 0));
			Block block = stateDown.getBlock();

			if (block == this && getMetaFromState(stateDown) != 0 || block.isFlammable(world, pos.add(0, densityDir, 0), EnumFacing.UP)) {
				world.setBlockState(pos.add(0, densityDir, 0), getDefaultState(), 2);
				world.setBlockToAir(pos);
				return;
			}
		}
		super.updateTick(world, pos, state, rand);
	}

	protected void checkForInteraction(World world, BlockPos pos, IBlockState state) {

		if (state.getBlock() != this) {
			return;
		}
		interactWithBlock(world, pos.add(0, -1, 0));
		interactWithBlock(world, pos.add(0, 1, 0));
		interactWithBlock(world, pos.add(-1, 0, 0));
		interactWithBlock(world, pos.add(1, 0, 0));
		interactWithBlock(world, pos.add(0, 0, -1));
		interactWithBlock(world, pos.add(0, 0, 1));

		interactWithBlock(world, pos.add(-1, 0, -1));
		interactWithBlock(world, pos.add(-1, 0, 1));
		interactWithBlock(world, pos.add(1, 0, -1));
		interactWithBlock(world, pos.add(1, 0, 1));
	}

	protected void interactWithBlock(World world, BlockPos pos) {

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (block.isAir(world, pos) || block == this) {
			return;
		}
		int bMeta = block.getMetaFromState(state);
		BlockWrapper result;

		if (hasInteraction(block, bMeta)) {
			result = getInteraction(block, bMeta);
			world.setBlockState(pos, block.getStateFromMeta(bMeta), 2);
			triggerInteractionEffects(world, pos);
		} else if (block.isFlammable(world, pos, EnumFacing.UP)) {
			world.setBlockState(pos, Blocks.fire.getDefaultState(), 2);
		} else if (world.isSideSolid(pos, EnumFacing.UP) && world.isAirBlock(pos.add(0, 1, 0))) {
			world.setBlockState(pos.add(0, 1, 0), Blocks.fire.getDefaultState(), 2);
		}
	}

	protected void triggerInteractionEffects(World world, BlockPos pos) {

		if (world.rand.nextInt(16) == 0) {
			world.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, "random.fizz", 0.5F,
					2.2F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
		}
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		GameRegistry.registerBlock(this, "FluidPyrotheum");

		addInteraction(Blocks.cobblestone, Blocks.stone);
		addInteraction(Blocks.grass, Blocks.dirt);
		addInteraction(Blocks.sand, Blocks.glass);
		addInteraction(Blocks.water, Blocks.stone);
		addInteraction(Blocks.flowing_water, Blocks.stone);
		addInteraction(Blocks.clay, Blocks.hardened_clay);
		addInteraction(Blocks.ice, Blocks.stone);
		addInteraction(Blocks.snow, Blocks.air);
		addInteraction(Blocks.snow_layer, Blocks.air);

		for (int i = 0; i < 8; i++) {
			addInteraction(Blocks.stone_stairs, i, Blocks.stone_brick_stairs, i);
		}

		String category = "Fluid.Pyrotheum";
		String comment = "Enable this for Fluid Pyrotheum to be worse than lava.";
		effect = ThermalFoundation.CONFIG.get(category, "Effect", true, comment);

		comment = "Enable this for Fluid Pyrotheum Source blocks to gradually fall downwards.";
		enableSourceFall = ThermalFoundation.CONFIG.get(category, "Fall", enableSourceFall, comment);

		return true;
	}

}
