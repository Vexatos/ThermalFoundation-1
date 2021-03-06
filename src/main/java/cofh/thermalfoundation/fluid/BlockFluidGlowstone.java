package cofh.thermalfoundation.fluid;

import cofh.core.fluid.BlockFluidCore;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalfoundation.ThermalFoundation;
import cofh.thermalfoundation.init.TFFluids;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemBlock;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Random;

public class BlockFluidGlowstone extends BlockFluidCore {

	public static final int LEVELS = 6;
	public static final Material materialFluidGlowstone = new MaterialLiquid(MapColor.YELLOW);

	private static boolean effect = true;
	private static boolean enableSourceCondense = true;
	private static boolean enableSourceFloat = true;
	private static int maxHeight = 120;

	public BlockFluidGlowstone(Fluid fluid) {

		super(fluid, materialFluidGlowstone, "thermalfoundation", "glowstone");
		setQuantaPerBlock(LEVELS);
		setTickRate(10);

		setHardness(1F);
		setLightOpacity(0);
		setParticleColor(1.0F, 0.9F, 0.05F);
	}

	public static void config() {

		String category = "Fluid.Glowstone";
		String comment;

		comment = "If TRUE, Fluid Glowstone will provide buffs to entities on contact.";
		effect = ThermalFoundation.CONFIG.getConfiguration().getBoolean("Effect", category, effect, comment);

		comment = "If TRUE, Fluid Glowstone Source blocks will condense back into solid Glowstone above a given y-value.";
		enableSourceCondense = ThermalFoundation.CONFIG.getConfiguration().getBoolean("Condense", category, enableSourceCondense, comment);

		comment = "If TRUE, Fluid Glowstone Source blocks will gradually float upwards.";
		enableSourceFloat = ThermalFoundation.CONFIG.getConfiguration().getBoolean("Float", category, enableSourceFloat, comment);

		comment = "This adjusts the y-value where Fluid Glowstone will *always* condense, if that is enabled. It will also condense above 80% of this value, if it cannot flow.";
		maxHeight = ThermalFoundation.CONFIG.getConfiguration().getInt("MaxHeight", category, maxHeight, maxHeight / 2, maxHeight * 2, comment);
	}

	private boolean shouldSourceBlockCondense(World world, BlockPos pos) {

		return enableSourceCondense && (pos.getY() + densityDir > maxHeight || pos.getY() + densityDir > world.getHeight() || pos.getY() + densityDir > maxHeight * 0.8F && !canDisplace(world, pos.add(0, densityDir, 0)));
	}

	private boolean shouldSourceBlockFloat(World world, BlockPos pos) {

		IBlockState state = world.getBlockState(pos.add(0, densityDir, 0));
		return enableSourceFloat && (state.getBlock() == this && state.getBlock().getMetaFromState(state) != 0);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		if (!effect) {
			return;
		}
		if (entity instanceof EntityLivingBase) {
			if (entity.motionY < -0.2) {
				entity.motionY *= 0.5;
				if (entity.fallDistance > 20) {
					entity.fallDistance = 20;
				} else {
					entity.fallDistance *= 0.95;
				}
			}
		}
		if (ServerHelper.isClientWorld(world)) {
			return;
		}
		if (world.getTotalWorldTime() % 8 == 0 && entity instanceof EntityLivingBase && !((EntityLivingBase) entity).isEntityUndead()) {
			((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SPEED, 6 * 20, 0));
			((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 6 * 20, 0));
		}
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {

		return TFFluids.fluidGlowstone.getLuminosity();
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if (getMetaFromState(state) == 0) {
			if (rand.nextInt(3) == 0) {
				if (shouldSourceBlockCondense(world, pos)) {
					world.setBlockState(pos, Blocks.GLOWSTONE.getDefaultState());
					return;
				}
				if (shouldSourceBlockFloat(world, pos)) {
					world.setBlockState(pos.add(0, densityDir, 0), this.getDefaultState(), 3);//TODO, is default state meta 0?
					world.setBlockToAir(pos);
					return;
				}
			}
		} else if (pos.getY() + densityDir > maxHeight) {
			int quantaRemaining = quantaPerBlock - getMetaFromState(state);
			int expQuanta;
			int y2 = pos.getY() - densityDir;

			if (world.getBlockState(pos.add(0, y2, 0)).getBlock() == this || world.getBlockState(pos.add(-1, y2, 0)).getBlock() == this || world.getBlockState(pos.add(1, y2, 0)).getBlock() == this || world.getBlockState(pos.add(0, y2, -1)).getBlock() == this || world.getBlockState(pos.add(0, y2, 1)).getBlock() == this) {
				expQuanta = quantaPerBlock - 1;
			} else {
				int maxQuanta = -100;
				maxQuanta = getLargerQuanta(world, pos.add(-1, 0, 0), maxQuanta);
				maxQuanta = getLargerQuanta(world, pos.add(1, 0, 0), maxQuanta);
				maxQuanta = getLargerQuanta(world, pos.add(0, 0, -1), maxQuanta);
				maxQuanta = getLargerQuanta(world, pos.add(0, 0, 1), maxQuanta);

				expQuanta = maxQuanta - 1;
			}
			// decay calculation
			if (expQuanta != quantaRemaining) {
				if (expQuanta <= 0) {
					world.setBlockToAir(pos);
				} else {
					world.setBlockState(pos, getDefaultState().withProperty(LEVEL, quantaPerBlock - expQuanta), 3);
					world.scheduleBlockUpdate(pos, this, tickRate, 0);
					world.notifyNeighborsOfStateChange(pos, this);
				}
			}
			return;
		}
		super.updateTick(world, pos, state, rand);
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		this.setRegistryName("fluid_glowstone");
		GameRegistry.register(this);
		ItemBlock itemBlock = new ItemBlock(this);
		itemBlock.setRegistryName(this.getRegistryName());
		GameRegistry.register(itemBlock);

		config();

		return true;
	}

}
