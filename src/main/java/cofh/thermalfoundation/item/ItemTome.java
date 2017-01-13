package cofh.thermalfoundation.item;

import cofh.api.core.IInitializer;
import cofh.api.item.IInventoryContainerItem;
import cofh.api.item.IMultiModeItem;
import cofh.core.item.ItemCoFHBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.KeyBindingMultiMode;
import cofh.core.util.StateMapper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalfoundation.ThermalFoundation;
import cofh.thermalfoundation.gui.GuiHandler;
import cofh.thermalfoundation.util.LexiconManager;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

import static cofh.lib.util.helpers.ItemHelper.ShapedRecipe;
import static cofh.lib.util.helpers.ItemHelper.addRecipe;

public class ItemTome extends ItemCoFHBase implements IInitializer, IInventoryContainerItem, IMultiModeItem {

	public ItemTome() {

		super("thermalfoundation");

		setUnlocalizedName("tome");
		setCreativeTab(ThermalFoundation.tabCommon);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		ItemStack lexicon = new ItemStack(item, 1, 0);
		setMode(lexicon, 0);
		list.add(lexicon);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean check) {

		if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			list.add(StringHelper.shiftForDetails());
		}
		if (!StringHelper.isShiftKeyDown()) {
			return;
		}
		switch (Type.values()[ItemHelper.getItemDamage(stack)]) {
			case LEXICON:
				list.add(StringHelper.getInfoText("info.thermalfoundation.tome.lexicon.0"));

				if (isActive(stack)) {
					list.add(StringHelper.localize("info.thermalfoundation.tome.lexicon.3") + StringHelper.END);
					list.add(StringHelper.YELLOW + StringHelper.ITALIC + StringHelper.localize("info.cofh.press") + " " + StringHelper.getKeyName(KeyBindingMultiMode.instance.getKey()) + " " + StringHelper.localize("info.thermalfoundation.tome.lexicon.4") + StringHelper.END);
				} else {
					list.add(StringHelper.localize("info.thermalfoundation.tome.lexicon.1") + StringHelper.END);
					list.add(StringHelper.BRIGHT_BLUE + StringHelper.ITALIC + StringHelper.localize("info.cofh.press") + " " + StringHelper.getKeyName(KeyBindingMultiMode.instance.getKey()) + " " + StringHelper.localize("info.thermalfoundation.tome.lexicon.2") + StringHelper.END);
				}
				break;
			default:
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrentItem) {

		if (!isActive(stack)) {
			return;
		}
		switch (Type.values()[ItemHelper.getItemDamage(stack)]) {
			case LEXICON:
				NBTTagCompound tag = entity.getEntityData();
				tag.setLong("cofh.LexiconUpdate", entity.worldObj.getTotalWorldTime());
				break;
			default:
		}
	}

	@Override
	public boolean isFull3D() {

		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		// TODO: OMNOMNOMICON
		if (isActive(stack)) {
			return "item.thermalfoundation.tome.lexicon.active";
		}
		return "item.thermalfoundation.tome.lexicon";
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		if (isActive(stack)) {
			return EnumRarity.RARE;
		}
		return EnumRarity.UNCOMMON;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {

		if (CoreUtils.isFakePlayer(player)) {
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}
		if (ServerHelper.isServerWorld(world) && LexiconManager.getSortedOreNames().size() > 0) {
			if (isActive(stack)) {
				player.openGui(ThermalFoundation.instance, GuiHandler.LEXICON_TRANSMUTE_ID, world, 0, 0, 0);
			} else {
				player.openGui(ThermalFoundation.instance, GuiHandler.LEXICON_STUDY_ID, world, 0, 0, 0);
			}
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	private boolean isActive(ItemStack stack) {

		return getMode(stack) == 1;
	}

	/* IModelRegister */
	@Override
	@SideOnly (Side.CLIENT)
	public void registerModels() {

		StateMapper mapper = new StateMapper(modName, "util", name);
		ModelBakery.registerItemVariants(this);
		ModelLoader.setCustomMeshDefinition(this, mapper);

		for (Map.Entry<Integer, ItemEntry> entry : itemMap.entrySet()) {
			ModelLoader.setCustomModelResourceLocation(this, entry.getKey(), new ModelResourceLocation(modName + ":" + "util", "type=" + entry.getValue().name));
		}
	}

	/* IInventoryContainerItem */
	@Override
	public int getSizeInventory(ItemStack container) {

		return 3;
	}

	/* IMultiModeItem */
	@Override
	public int getMode(ItemStack stack) {

		return !stack.hasTagCompound() ? 0 : stack.getTagCompound().getInteger("Mode");
	}

	@Override
	public boolean setMode(ItemStack stack, int mode) {

		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("Mode", mode);
		return false;
	}

	@Override
	public boolean incrMode(ItemStack stack) {

		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		int curMode = getMode(stack);
		curMode++;
		if (curMode >= getNumModes(stack)) {
			curMode = 0;
		}
		stack.getTagCompound().setInteger("Mode", curMode);
		return true;
	}

	@Override
	public boolean decrMode(ItemStack stack) {

		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		int curMode = getMode(stack);
		curMode--;
		if (curMode <= 0) {
			curMode = getNumModes(stack) - 1;
		}
		stack.getTagCompound().setInteger("Mode", curMode);
		return true;
	}

	@Override
	public int getNumModes(ItemStack stack) {

		return 2;
	}

	@Override
	public void onModeChange(EntityPlayer player, ItemStack stack) {

		if (isActive(stack)) {
			player.worldObj.playSound(player.posX, player.posY, player.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 0.4F, 1.0F, false);
		} else {
			player.worldObj.playSound(player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.6F, false);
		}
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		lexicon = addItem(0, "lexicon");

		return true;
	}

	@Override
	public boolean initialize() {

		return true;
	}

	@Override
	public boolean postInit() {

		addRecipe(ShapedRecipe(lexicon, new Object[] { " D ", "GBI", " R ", 'D', "gemDiamond", 'G', "ingotGold", 'B', Items.BOOK, 'I', "ingotIron", 'R', "dustRedstone" }));

		return true;
	}

	/* REFERENCES */
	public static ItemStack lexicon;

	/* TYPE */
	enum Type {
		LEXICON, OMNOMNOMICON
	}

}