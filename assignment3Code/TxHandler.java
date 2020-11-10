import java.util.ArrayList;
import java.util.Arrays;

public class TxHandler {

    private final UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        pool = new UTXOPool(utxoPool);
    }

    public UTXOPool getUTXOPool(){
        return pool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        double sum_outputs = 0;
        double sum_inputs = 0;
        for (Transaction.Output output : tx.getOutputs()){
            if (output.value < 0)
                return false;
            sum_outputs = sum_outputs + output.value;
        }
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<UTXO> seen_utxos = new ArrayList<>();
        for (int i = 0; i < inputs.size(); ++i){
            UTXO utxo = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
            if (!pool.contains(utxo) || seen_utxos.contains(utxo)){
                return false;
            }
            seen_utxos.add(utxo);
            Transaction.Output output = pool.getTxOutput(utxo);
            byte[] message = tx.getRawDataToSign(i);
            if(!Crypto.verifySignature(output.address, message, inputs.get(i).signature))
                return false;
            sum_inputs = sum_inputs + output.value;
        }
        return sum_inputs >= sum_outputs;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> valid_transactions = new ArrayList<>();
        for(Transaction transaction : possibleTxs){
            if (this.isValidTx(transaction))
                valid_transactions.add(transaction);
                for (Transaction.Input input : transaction.getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    pool.removeUTXO(utxo);
                }
                for (int i = 0; i < transaction.numOutputs(); ++i){
                    UTXO utxo = new UTXO(transaction.getHash(), i);
                    pool.addUTXO(utxo, transaction.getOutput(i));
                }
        }
        Transaction[] transactions = new Transaction[valid_transactions.size()];
        return valid_transactions.toArray(transactions);
    }

}
