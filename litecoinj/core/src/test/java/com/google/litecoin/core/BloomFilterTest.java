package com.google.litecoin.core;

import com.google.litecoin.params.MainNetParams;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

import static org.junit.Assert.*;

public class BloomFilterTest {
    @Test
    public void insertSerializeTest() {
        BloomFilter filter = new BloomFilter(3, 0.01, 0, BloomFilter.BloomUpdate.UPDATE_ALL);
        
        filter.insert(Hex.decode("99108ad8ed9bb6274d3980bab5a85c048f0950c8"));
        assertTrue (filter.contains(Hex.decode("99108ad8ed9bb6274d3980bab5a85c048f0950c8")));
        // One bit different in first byte
        assertFalse(filter.contains(Hex.decode("19108ad8ed9bb6274d3980bab5a85c048f0950c8")));

        filter.insert(Hex.decode("b5a2c786d9ef4658287ced5914b37a1b4aa32eee"));
        assertTrue(filter.contains(Hex.decode("b5a2c786d9ef4658287ced5914b37a1b4aa32eee")));
        
        filter.insert(Hex.decode("b9300670b4c5366e95b2699e8b18bc75e5f729c5"));
        assertTrue(filter.contains(Hex.decode("b9300670b4c5366e95b2699e8b18bc75e5f729c5")));
        
        // Value generated by the reference client
        assertTrue(Arrays.equals(Hex.decode("03614e9b050000000000000001"), filter.bitcoinSerialize()));
    }
    
    @Test
    public void insertSerializeTestWithTweak() {
        BloomFilter filter = new BloomFilter(3, 0.01, 2147483649L);
        
        filter.insert(Hex.decode("99108ad8ed9bb6274d3980bab5a85c048f0950c8"));
        assertTrue (filter.contains(Hex.decode("99108ad8ed9bb6274d3980bab5a85c048f0950c8")));
        // One bit different in first byte
        assertFalse(filter.contains(Hex.decode("19108ad8ed9bb6274d3980bab5a85c048f0950c8")));

        filter.insert(Hex.decode("b5a2c786d9ef4658287ced5914b37a1b4aa32eee"));
        assertTrue(filter.contains(Hex.decode("b5a2c786d9ef4658287ced5914b37a1b4aa32eee")));
        
        filter.insert(Hex.decode("b9300670b4c5366e95b2699e8b18bc75e5f729c5"));
        assertTrue(filter.contains(Hex.decode("b9300670b4c5366e95b2699e8b18bc75e5f729c5")));
        
        // Value generated by the reference client
        assertTrue(Arrays.equals(Hex.decode("03ce4299050000000100008002"), filter.bitcoinSerialize()));
    }

    @Test
    public void walletTest() throws Exception {
        NetworkParameters params = MainNetParams.get();

        DumpedPrivateKey privKey = new DumpedPrivateKey(params, "5Kg1gnAjaLfKiwhhPpGS3QfRg2m6awQvaj98JCZBZQ5SuS2F15C");
        
        Address addr = privKey.getKey().toAddress(params);
        assertTrue(addr.toString().equals("17Wx1GQfyPTNWpQMHrTwRSMTCAonSiZx9e"));
        
        Wallet wallet = new Wallet(params);
        // Check that the wallet was created with no keys
        // If wallets ever get created with keys, this test needs redone.
        for (ECKey key : wallet.getKeys())
            fail();
        wallet.addKey(privKey.getKey());
        // Add a random key which happens to have been used in a recent generation
        wallet.addKey(new ECKey(null, Hex.decode("03cb219f69f1b49468bd563239a86667e74a06fcba69ac50a08a5cbc42a5808e99")));
        wallet.commitTx(new Transaction(params, Hex.decode("01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0d038754030114062f503253482fffffffff01c05e559500000000232103cb219f69f1b49468bd563239a86667e74a06fcba69ac50a08a5cbc42a5808e99ac00000000")));
        
        // We should have 2 per pubkey, and one for the pay-2-pubkey output we have
        assertTrue(wallet.getBloomFilterElementCount() == 5);
        
        BloomFilter filter = wallet.getBloomFilter(wallet.getBloomFilterElementCount(), 0.001, 0);
        
        // Value generated by the reference client
        assertTrue(Arrays.equals(Hex.decode("082ae5edc8e51d4a03080000000000000002"), filter.bitcoinSerialize()));
    }
}
