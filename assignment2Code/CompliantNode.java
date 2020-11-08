import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private Set<Transaction> seenTransactions;
    private Set<Transaction> pendingTransactions;
    private int total_rounds;
    private int round;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        pendingTransactions = new HashSet<>();
        seenTransactions = new HashSet<>();
        round = 0;
        total_rounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        round = round + 1;
        HashSet<Transaction> to_send = new HashSet<>(pendingTransactions);
        pendingTransactions = new HashSet<>();
        return (round == total_rounds + 1) ? seenTransactions : to_send;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        for(Candidate candidate : candidates){
            boolean seen = false;
            for (Transaction seenTransaction : seenTransactions) {

                if (seenTransaction.equals(candidate.tx)) {
                    seen = true;
                    break;
                }
            }
            if (!seen){
                pendingTransactions.add(candidate.tx);
                seenTransactions.add(candidate.tx);
            }
        }
    }
}
