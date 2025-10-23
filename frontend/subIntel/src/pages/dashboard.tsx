import { useEffect, useState } from "react";
import LinkAccount from "../components/LinkAccount";

interface Transaction {
  id: number;
  name: string;
  amount: number;
  date: string;
  category: string;
  accountName: string;
}

interface Subscription {
  id: number;
  merchantName: string;
  estimatedAmount: number;
  frequency: string;
  nextDueDate: string;
  lastPaymentDate: string;
  isActive: boolean;
}

export const Dashboard = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [isSubLoading, setIsSubLoading] = useState(false);

  const fetchSubscriptions = async () => {
    setIsSubLoading(true);
    try {
      const jwtToken = localStorage.getItem("jwtToken");
      const response = await fetch("http://localhost:8080/api/subscriptions", {
        headers: { Authorization: `Bearer ${jwtToken}` },
      });
      const data = await response.json();
      setSubscriptions(data);
    } catch (error) {
      console.error("Error fetching subscriptions:", error);
    } finally {
      setIsSubLoading(false);
    }
  };

  useEffect(() => {
    fetchSubscriptions();
  }, []);

  const handleSyncTransactions = async () => {
    setIsLoading(true);
    setTransactions([]);
    try {
      const jwtToken = localStorage.getItem("jwtToken");

      const syncResponse = await fetch(
        "http://localhost:8080/api/plaid/transactions",
        {
          method: "POST",
          headers: { Authorization: `Bearer ${jwtToken}` },
        }
      );

      if (!syncResponse.ok) {
        throw new Error("Failed to trigger transaction sync");
      }

      await fetch("http://localhost:8080/api/subscriptions/detect", {
        method: "POST",
        headers: { Authorization: `Bearer ${jwtToken}` },
      });

      const transResponse = await fetch(
        "http://localhost:8080/api/transactions",
        {
          headers: { Authorization: `Bearer ${jwtToken}` },
        }
      );
      if (transResponse.ok) {
        const data = await transResponse.json();
        setTransactions(data);
      } else {
        console.error("Failed to fetch transactions after sync");
      }
      await fetchSubscriptions();
    } catch (error) {
      console.error("Error during sync process:", error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <h1>Dashboard</h1>
      <LinkAccount />
      <hr />
      <button
        onClick={handleSyncTransactions}
        disabled={isLoading || isSubLoading}
      >
        {isLoading ? "Syncing..." : "Sync Transactions"}
      </button>

      <h2>Subscriptions</h2>
      {isSubLoading ? (
        <p>Loading subscriptions...</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Merchant</th>
              <th>Est. Amount</th>
              <th>Frequency</th>
              <th>Last Payment</th>
              <th>Next Due</th>
            </tr>
          </thead>
          <tbody>
            {subscriptions.length === 0 && (
              <tr>
                <td colSpan={5}>No subscriptions detected.</td>
              </tr>
            )}
            {subscriptions.map((sub) => (
              <tr key={sub.id} style={{ opacity: sub.isActive ? 1 : 0.5 }}>
                <td>{sub.merchantName}</td>
                <td>${Math.abs(sub.estimatedAmount).toFixed(2)}</td>
                <td>{sub.frequency}</td>
                <td>{sub.lastPaymentDate}</td>
                <td>{sub.nextDueDate}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <h2>Transactions</h2>
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Name</th>
            <th>Amount</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((t) => (
            <tr key={t.id}>
              <td>{t.date}</td>
              <td>{t.name}</td>
              <td>${t.amount.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

/* export const Dashboard=() =>{
  return (
    <div>
      <h1>dashboard</h1>
      <LinkAccount />
    </div>
  );
} */
