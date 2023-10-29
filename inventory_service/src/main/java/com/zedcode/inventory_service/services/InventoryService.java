package com.zedcode.inventory_service.services;

import com.zedcode.inventory_service.model.dtos.BaseResponse;
import com.zedcode.inventory_service.model.dtos.OrderItemRequest;
import com.zedcode.inventory_service.model.entities.Inventory;
import com.zedcode.inventory_service.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public boolean isInStock(String sku){
        var inventory = inventoryRepository.findBySku(sku);

        return inventory.filter(value -> value.getQuantity() > 0).isPresent();
    }

    public BaseResponse areInStock(List<OrderItemRequest> orderItemRequestList){
        var errorList = new ArrayList<String>();

        List<String> skus = orderItemRequestList.stream().map(OrderItemRequest::getSku).toList();

        List<Inventory> inventoryList = inventoryRepository.findBySkuIn(skus);

        orderItemRequestList.forEach(orderItemRequest -> {
            var invetory = inventoryList.stream().filter(value -> value.getSku().equals(orderItemRequest.getSku())).findFirst();
            if(invetory.isEmpty()){
                errorList.add("Product with sku "+orderItemRequest.getSku() + "does not exist");
            } else if (invetory.get().getQuantity() < orderItemRequest.getQuantity()) {
                errorList.add("Product with sku "+orderItemRequest.getSku() + "has insufficient quantity");
            }
        });

        return !errorList.isEmpty() ? new BaseResponse(errorList.toArray(new String[0])) : new BaseResponse(null);
    }

}
