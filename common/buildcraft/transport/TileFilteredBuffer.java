package buildcraft.transport;

import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.SimpleInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileFilteredBuffer extends TileBuildCraft implements IInventory {

	private final SimpleInventory inventoryFilters = new SimpleInventory(9, "FilteredBufferFilters", 1);
	private final SimpleInventory inventoryStorage = new SimpleInventory(9, "FilteredBufferStorage", 64);

	@Override
	public void updateEntity() {
		super.updateEntity();
	}

	public IInventory getFilters() {
		return inventoryFilters;
	}

	/** IInventory Implementation **/

	@Override
	public int getSizeInventory() {
		return inventoryStorage.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return inventoryStorage.getStackInSlot(slotId);
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		return inventoryStorage.decrStackSize(slotId, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		return inventoryStorage.getStackInSlotOnClosing(slotId);
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemStack) {
		inventoryStorage.setInventorySlotContents(slotId, itemStack);
	}

	@Override
	public String getInventoryName() {
		return inventoryStorage.getInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		return inventoryStorage.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {

		ItemStack filterItemStack = inventoryFilters.getStackInSlot(i);

		if ( filterItemStack == null || filterItemStack.getItem() != itemstack.getItem())
			return false;

		if (itemstack.getItem().isDamageable())
			return true;

		if (filterItemStack.getItemDamage() == itemstack.getItemDamage())
			return true;

		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		NBTTagCompound inventoryStorageTag = (NBTTagCompound) nbtTagCompound.getTag("inventoryStorage");
		inventoryStorage.readFromNBT(inventoryStorageTag);
		NBTTagCompound inventoryFiltersTag = (NBTTagCompound) nbtTagCompound.getTag("inventoryFilters");
		inventoryFilters.readFromNBT(inventoryFiltersTag);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		NBTTagCompound inventoryStorageTag = new NBTTagCompound();
		inventoryStorage.writeToNBT(inventoryStorageTag);
		nbtTagCompound.setTag("inventoryStorage", inventoryStorageTag);

		NBTTagCompound inventoryFiltersTag = new NBTTagCompound();
		inventoryFilters.writeToNBT(inventoryFiltersTag);
		nbtTagCompound.setTag("inventoryFilters", inventoryFiltersTag);
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}
}
