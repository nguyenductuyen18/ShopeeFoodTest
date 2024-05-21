package com.example.shopeefood.controller;

import com.example.shopeefood.model.*;
import com.example.shopeefood.repository.IDetailCartRepository;
import com.example.shopeefood.repository.IUserRepository;
import com.example.shopeefood.service.cart.ICartService;
import com.example.shopeefood.service.detailcart.IDetailCartService;
import com.example.shopeefood.service.product.IProductService;
import com.example.shopeefood.service.shop.IShopService;
import com.example.shopeefood.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/detailCart")
public class DetailCartController {
    @Autowired
    private IDetailCartService iDetailCartService;
    @Autowired
    private IShopService iShopService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICartService iCartService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IDetailCartRepository idetailCartRepository;

    @GetMapping("/{idShop}/{idUser}")
    public ResponseEntity<List<DetailCart>> getDetailCart(@PathVariable Long idShop, @PathVariable Long idUser) {
        Optional<User> user = iUserService.findById(idUser);
        Optional<Shop> shop = iShopService.findById(idShop);
        List<DetailCart> detailCarts = (List<DetailCart>) iDetailCartService.findAllByShopAndCart(shop.get(),user.get());
        if (detailCarts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else {
            return new ResponseEntity<>(detailCarts, HttpStatus.OK);
        }
    }
    @PostMapping("/{idUser}/{idShop}/{idProduct}")
    public ResponseEntity<DetailCart> saveDetailCart(@PathVariable long idShop, @PathVariable long idUser, @PathVariable Long idProduct) {
        Optional<Shop> shop = iShopService.findById(idShop);
        Optional<Product> product = iProductService.findById(idProduct);
        Optional<Cart> cart = iCartService.findById(idUser);
        LocalDateTime currentDateTime = LocalDateTime.now();
        cart.get().setCreatedAt(currentDateTime);
        DetailCart detailCart = new DetailCart(product.get(),1,shop.get(),cart.get());
        iDetailCartService.save(detailCart);
        return new ResponseEntity<>(detailCart, HttpStatus.CREATED);
    }
    @PutMapping("/plus/{id}")
    public ResponseEntity<DetailCart> plusCart(@PathVariable Long id) {
        Optional<DetailCart>  detailCart=  idetailCartRepository.findById(id);
      int number=detailCart.get().getQuantity()+1;
      detailCart.get().setQuantity(number);
      return new ResponseEntity<>(iDetailCartService.save(detailCart.get()), HttpStatus.OK);
    }
    @PutMapping("/minus/{id}")
    public ResponseEntity<Void> minusCart(@PathVariable Long id) {
        Optional<DetailCart> detailCartOptional = idetailCartRepository.findById(id);
        if (!detailCartOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        DetailCart detailCart = detailCartOptional.get();
        int number = detailCart.getQuantity() - 1;
        if (number <= 0) {
            idetailCartRepository.delete(detailCart);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            detailCart.setQuantity(number);
            iDetailCartService.save(detailCart);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        Optional<DetailCart> detailCart = idetailCartRepository.findById(id);
        if (!detailCart.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        idetailCartRepository.delete(detailCart.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}