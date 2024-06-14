package com.example.shopeefood.controller;

import com.example.shopeefood.model.*;
import com.example.shopeefood.repository.*;
import com.example.shopeefood.service.detailcart.IDetailCartService;
import com.example.shopeefood.service.orderItem.IOrderItemService;
import com.example.shopeefood.service.shop.IShopService;
import com.example.shopeefood.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IShopService iShopService;
    @Autowired
    private IOrderItemService iOrderItemService;
    @Autowired
    private IOrderRepository iOrderRepository;
    @Autowired
    private IDetailCartService iDetailCartService;
    @Autowired
    private IStatusRepository iStatusRepository;
    @Autowired
    private IOrderItemRepository iOrderItemRepository;
    @Autowired
    private IAddressRepository iAddressRepository;
    @Autowired
    private IAddressOrderRepository addressOrderRepository;

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderItemsByOrderId(@PathVariable long orderId) {
        return  new ResponseEntity<>(iOrderRepository.findById(orderId).get(), HttpStatus.OK);

    }


    @GetMapping("/orderByShip")
    public ResponseEntity<List<Order>> getOrderByShip() {
        List<Order> orderList = iOrderRepository.findByStatusId(2);
        Collections.reverse(orderList);
            return new ResponseEntity<>(orderList, HttpStatus.OK);

    }
    @GetMapping("/orderReceived")
    public ResponseEntity<List<Order>> getOrderReceived() {
        List<Order> orderList = new ArrayList<>();
        List<Order> orderList1 = iOrderRepository.findByStatusId(4);
        Collections.reverse(orderList1);
        orderList.addAll(orderList1);
        List<Order> orderList2 = iOrderRepository.findByStatusId(6);
        Collections.reverse(orderList2);
        orderList.addAll(orderList2);
        List<Order> orderList3 = iOrderRepository.findByStatusId(7);
        Collections.reverse(orderList3);
        orderList.addAll(orderList3);

        return new ResponseEntity<>(orderList, HttpStatus.OK);

    }

    @GetMapping("/orderItem/{orderId}")
    public ResponseEntity<List<OrderItem>> getOrderItemByOrderId(@PathVariable long orderId) {
        List<OrderItem> orderItems = iOrderItemService.findByOrderId(orderId);
        if (orderItems.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else {
            return new ResponseEntity<>(orderItems, HttpStatus.OK);
        }
    }
    @PutMapping("/status/{idOrder}/{statusId}")
    public ResponseEntity<Order> setStatus(@PathVariable long idOrder, @PathVariable long statusId) {
        Optional<Order> optionalOrder = iOrderRepository.findById(idOrder);
        if (!optionalOrder.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Status> optionalStatus = iStatusRepository.findById(statusId);
        if (!optionalStatus.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        Order order = optionalOrder.get();
        Status status = optionalStatus.get();
        order.setStatus(status);
        iOrderRepository.save(order);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/shop/{shopId}")
    public ResponseEntity<List<Order>> getOrdersByShopId(@PathVariable long shopId) {
        List<Order> orderList = iOrderRepository.findByOrderItems_Shop_Id(shopId);
        Collections.reverse(orderList);
        return new ResponseEntity<>(orderList,HttpStatus.OK);
    }
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable long userId) {
        List<Order> orderList = iOrderRepository.findByUserId(userId);
        Collections.reverse(orderList);
        return new ResponseEntity<>(orderList,HttpStatus.OK);
    }

    @GetMapping("/status/{statusId}")
    public ResponseEntity<List<Order>> getOrdersByStatusId(@PathVariable long statusId) {
        if(statusId==1L){
            List<Order> orderList =iOrderRepository.findAll();
            Collections.reverse(orderList);
            return new ResponseEntity<>(orderList,HttpStatus.OK);
        }
        else {
            List<Order> orderList =iOrderRepository.findByStatusId(statusId);
            Collections.reverse(orderList);
            return new ResponseEntity<>(orderList,HttpStatus.OK);
        }

    }




    @PostMapping("/{idUser}/{idShop}/{idAddress}")
    public ResponseEntity<Order> createOrder(@PathVariable long idShop, @PathVariable long idUser, @PathVariable long idAddress, @RequestBody String note) {
        Optional<User> userOptional = iUserService.findById(idUser);
        Optional<Shop> shopOptional = iShopService.findById(idShop);

        Optional<Address> addressOptional = iAddressRepository.findById(idAddress);


        Address a = iAddressRepository.findById(idAddress).get();

        User user = userOptional.get();
        Shop shop = shopOptional.get();
        AddressOrder address = new AddressOrder(a.getPhoneNumber(), a.getAddress(),a.getNameUser());
        addressOrderRepository.save(address);
        List<OrderItem> orderItems = (List<OrderItem>) iOrderItemService.findAllByShopAndCart(shop, user);
        if (orderItems.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Status> statusOptional = iStatusRepository.findById(1L);
        if (!statusOptional.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        Order order = new Order();
        order.setStatus(statusOptional.get());
        order.setUser(user);
        order.setAddressOrder(address);
        for (OrderItem item : orderItems) {
            if (item.getOrder() == null) {
                item.setNote(note);
                order.addOrderItem(item);
            }
        }


        Order savedOrder = iOrderRepository.save(order);
        for (OrderItem orderItem : orderItems) {

            iDetailCartService.remove(orderItem.getId());
        }

        return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
    }

}
