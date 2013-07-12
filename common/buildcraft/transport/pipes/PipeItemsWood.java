/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.transport.IPipedItem;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.inventory.InventoryWrapper;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsWood extends Pipe implements IPowerReceptor {

	protected PowerHandler powerHandler;
	protected int standardIconIndex = PipeIconProvider.TYPE.PipeItemsWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();

	protected PipeItemsWood(PipeTransportItems transport, PipeLogic logic, int itemID) {
		super(transport, logic, itemID);

		powerHandler = new PowerHandler(this, Type.MACHINE);
		powerHandler.configure(1, 64, 1, 64);
		powerHandler.configurePowerPerdition(0, 0);
	}

	protected PipeItemsWood(int itemID, PipeTransportItems transport) {
		this(transport, new PipeLogicWood(), itemID);
	}

	public PipeItemsWood(int itemID) {
		this(itemID, new PipeTransportItems());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return standardIconIndex;
		else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == direction.ordinal())
				return solidIconIndex;
			else
				return standardIconIndex;
		}
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
		if (powerHandler.getEnergyStored() <= 0)
			return;

		extractItems();
		powerHandler.setEnergy(0);
	}

	private void extractItems() {
		int meta = container.getBlockMetadata();

		if (meta > 5)
			return;
		
		Position pos = new Position(xCoord, yCoord, zCoord, ForgeDirection.getOrientation(meta));
		pos.moveForwards(1);
		TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

		if (tile instanceof IInventory) {
			if (!PipeManager.canExtractItems(this, worldObj, (int) pos.x, (int) pos.y, (int) pos.z))
				return;

			IInventory inventory = (IInventory) tile;

			ItemStack[] extracted = checkExtract(inventory, true, pos.orientation.getOpposite());
			if (extracted == null)
				return;

			for (ItemStack stack : extracted) {
				if (stack == null || stack.stackSize == 0) {
					powerHandler.useEnergy(1, 1, true);
					continue;
				}

				Position entityPos = new Position(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, pos.orientation.getOpposite());

				entityPos.moveForwards(0.6);

				IPipedItem entity = new EntityPassiveItem(worldObj, entityPos.x, entityPos.y, entityPos.z, stack);

				((PipeTransportItems) transport).entityEntering(entity, entityPos.orientation);
			}
		}
	}

	/**
	 * Return the itemstack that can be if something can be extracted from this
	 * inventory, null if none. On certain cases, the extractable slot depends
	 * on the position of the pipe.
	 */
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, ForgeDirection from) {

		/* ISPECIALINVENTORY */
		if (inventory instanceof ISpecialInventory) {
			ItemStack[] stacks = ((ISpecialInventory) inventory).extractItem(doRemove, from, (int) powerHandler.getEnergyStored());
			if (stacks != null && doRemove) {
				for (ItemStack stack : stacks) {
					if (stack != null) {
						powerHandler.useEnergy(stack.stackSize, stack.stackSize, true);
					}
				}
			}
			return stacks;
		} else {

			IInventory inv = Utils.getInventory(inventory);
			ItemStack result = checkExtractGeneric(inv, doRemove, from);

			if (result != null)
				return new ItemStack[]{result};
		}

		return null;

	}

	public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, ForgeDirection from) {
		return checkExtractGeneric(InventoryWrapper.getWrappedInventory(inventory), doRemove, from);
	}

	public ItemStack checkExtractGeneric(ISidedInventory inventory, boolean doRemove, ForgeDirection from) {
		for (int k : inventory.getAccessibleSlotsFromSide(from.ordinal())) {
			ItemStack slot = inventory.getStackInSlot(k);

			if (slot != null && slot.stackSize > 0 && inventory.canExtractItem(k, slot, from.ordinal())) {
				if (doRemove) {
					return inventory.decrStackSize(k, (int) powerHandler.useEnergy(1, slot.stackSize, true));
				} else {
					return slot;
				}
			}
		}

		return null;
	}
}
