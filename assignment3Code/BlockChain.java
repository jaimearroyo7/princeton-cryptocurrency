// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.HashMap;
import java.util.Map;

public class BlockChain {

    public class BlockInfo {
        public Block block;
        public int height;
        public UTXOPool utxoPool;
        public long date;
        public BlockInfo(Block b, UTXOPool utxoPool, int prev_height){
            block = b;
            height = prev_height + 1;
            this.utxoPool = new UTXOPool(utxoPool);
            date = System.currentTimeMillis();
        }

    }

    public static final int CUT_OFF_AGE = 10;

    private TransactionPool t_pool;

    private HashMap<byte[], BlockInfo> blocks;

    private Block maxHeightBlock;
    private int maxHeightChain;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        t_pool = new TransactionPool();
        blocks = new HashMap<>();
        BlockInfo genesisInfo = new BlockInfo(genesisBlock, new UTXOPool(), 2);
        UTXO utxo = new UTXO(genesisBlock.getCoinbase().getHash(), 0);
        genesisInfo.utxoPool.addUTXO(utxo, genesisBlock.getCoinbase().getOutput(0));
        blocks.put(genesisBlock.getHash(), genesisInfo);
        update_max_height();
    }

    public void update_max_height(){
        Block result = null;
        int max_height = -1;
        long max_date = -1;
        for (Map.Entry m : blocks.entrySet()) {
            BlockInfo blockInfo = (BlockInfo) m.getValue();
            if (max_height < blockInfo.height || (max_height == blockInfo.height && max_date < blockInfo.date)){
                result = blockInfo.block;
                max_height = blockInfo.height;
                max_date = blockInfo.date;
            }
        }
        maxHeightBlock = result;
        maxHeightChain = max_height;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return maxHeightBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return blocks.get(maxHeightBlock.getHash()).utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return t_pool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        BlockInfo prev_block = blocks.get(block.getPrevBlockHash());
        if(prev_block == null || (prev_block.height + 1 <= maxHeightChain - CUT_OFF_AGE)) return false;
        BlockInfo new_block = new BlockInfo(block, prev_block.utxoPool, prev_block.height);
        Transaction[] total_transactions = block.getTransactions().toArray(new Transaction[0]);
        TxHandler handler = new TxHandler(prev_block.utxoPool);
        Transaction[] valid_transactions = handler.handleTxs(total_transactions);
        if(total_transactions.length != valid_transactions.length) return false;
        for(Transaction t : block.getTransactions()) {
            t_pool.removeTransaction(t.getHash());
            for (Transaction.Input input : t.getInputs()) {
                UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                new_block.utxoPool.removeUTXO(utxo);
            }
            for (int i = 0; i < t.numOutputs(); ++i) {
                UTXO utxo = new UTXO(t.getHash(), i);
                new_block.utxoPool.addUTXO(utxo, t.getOutput(i));
            }
        }
        blocks.put(block.getHash(), new_block);
        update_max_height();
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        t_pool.addTransaction(tx);
    }
}