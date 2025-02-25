package com.example.brokerage.controller;

import com.example.brokerage.entities.Asset;
import com.example.brokerage.exceptions.AssetNotFoundException;
import com.example.brokerage.service.AssetService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController extends MyCustomBaseController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    @RequestMapping("/all/{customerId}")
    public List<Asset> listAssets(@PathVariable Long customerId) {
        return executeIfLoggedUserAllowed(customerId, assetService::getAssetsByCustomerId);
    }

    @GetMapping
    @RequestMapping("/all/{customerId}/{assetName}")
    public Asset listAssets(@PathVariable Long customerId, @PathVariable String assetName) {
        if(isLoggedUserAllowed(customerId)){
        return assetService.getAssetByCustomerIdAndName(customerId, assetName)
                .orElseThrow(() -> new AssetNotFoundException("Asset not found"));
        }
        throw new AccessDeniedException("User was not allowed to access requested resource");
    }
}
