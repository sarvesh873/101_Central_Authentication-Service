package com.central.authentication_service.grpc;

import com.central.wallet.WalletCreateRequestGRPC;
import com.central.wallet.WalletResponseGRPC;
import com.central.wallet.WalletServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WalletServiceGrpcClient {

    private final WalletServiceGrpc.WalletServiceBlockingStub walletServiceBlockingStub;

    public WalletServiceGrpcClient(@Value("${wallet.service.host}") String walletServiceHost,
                                   @Value("${wallet.service.port}") int walletServicePort) {
        log.info("Initializing Connection to Wallet Service at {}:{}", walletServiceHost, walletServicePort );
        ManagedChannel channel = ManagedChannelBuilder.forAddress(walletServiceHost, walletServicePort)
                .usePlaintext()
                .build();
        this.walletServiceBlockingStub = WalletServiceGrpc.newBlockingStub(channel);
    }

    public void createWallet(String userCode, String currency) {

         WalletCreateRequestGRPC request = WalletCreateRequestGRPC.newBuilder()
                .setUserCode(userCode)
                .setCurrency(currency)
                .build();
        WalletResponseGRPC response = walletServiceBlockingStub.createWallet(request);
        log.info("Received response from wallet service via GRPC: {}", response);
    }

}
