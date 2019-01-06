package com.he.order.mapper;

import com.hand.hap.mybatis.common.Mapper;
import com.he.order.dto.InvInventoryItems;

import java.util.List;

public interface InvInventoryItemsMapper extends Mapper<InvInventoryItems>{
    List<InvInventoryItems> lovInvInventoryItems(InvInventoryItems inventoryItem);
    List<InvInventoryItems> lovInvInventoryItemsForOrderLines(InvInventoryItems invInventoryItem);
}