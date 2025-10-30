import { useEffect, useState } from "react";
import LinkAccount from "../components/LinkAccount";
import SubscriptionPieChart from "../components/SubscriptionPieChart";
import SubscriptionForm from "../components/SubscriptionForm";

interface Transaction {
  id: number;
  name: string;
  amount: number;
  date: string;
  category: string;
  accountName: string;
  accountId: string;
}

interface Subscription {
  subscriptionId: number;
  merchantName: string;
  estimatedAmount: number;
  frequency: string;
  nextDueDate: string;
  lastPaymentDate: string;
  isActive: boolean;
}

interface Account {
  accountId: string;
  name: string;
  officialName: string | null;
}

export const Dashboard = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [isSubLoading, setIsSubLoading] = useState(false);

  const [accounts, setAccounts] = useState<Account[]>([]);
  const [selectedAccountId, setSelectedAccountId] = useState<string>("all");
  const [isAccountsLoading, setIsAccountsLoading] = useState(false);

  const [showAddForm, setShowAddForm] = useState(false);

  const [editingSubscription, setEditingSubscription] =
    useState<Subscription | null>(null);

  const fetchAccounts = async () => {
    setIsAccountsLoading(true);
    try {
      const jwtToken = localStorage.getItem("jwtToken");
      const response = await fetch("http://localhost:8080/api/accounts", {
        headers: { Authorization: `Bearer ${jwtToken}` },
      });
      if (response.ok) {
        const data = await response.json();
        setAccounts(data);
      } else {
        console.error("Failed to fetch accounts");
        setAccounts([]);
      }
    } catch (error) {
      console.error("Error fetching accounts:", error);
    } finally {
      setIsAccountsLoading(false);
    }
  };

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
    fetchAccounts();
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

      await fetch("http://localhost:8080/api/subscriptions/detect", {
        method: "POST",
        headers: { Authorization: `Bearer ${jwtToken}` },
      });

      await fetchAccounts();
      await fetchSubscriptions();
    } catch (error) {
      console.error("Error during sync process:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAccountChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const accountId = e.target.value;
    setSelectedAccountId(accountId);
  };

  const handleSubscriptionAdded = () => {
    setShowAddForm(false);
    fetchSubscriptions();
  };

  const filteredTransactions = transactions.filter(
    (t) => selectedAccountId === "all" || t.accountId === selectedAccountId
  );

  const handleDeleteSubscription = async (subscriptionId: number) => {
    if (!window.confirm("Are you sure you want to delete this subscription?")) {
      return;
    }

    try {
      const jwtToken = localStorage.getItem("jwtToken");
      const response = await fetch(
        `http://localhost:8080/api/subscriptions/${subscriptionId}`,
        {
          method: "DELETE",
          headers: { Authorization: `Bearer ${jwtToken}` },
        }
      );
      if (response.ok) {
        fetchSubscriptions();
      } else {
        const errorData = await response.text();
        console.error("Failed to delete subscription:", errorData);
        alert(`Failed to delete: ${errorData}`);
      }
    } catch (error) {
      console.error("Error deleting subscription:", error);
      alert("An error occurred while deleting the subscription.");
    }
  };

  const handleSubscriptionUpdated = () => {
    setEditingSubscription(null);
    fetchSubscriptions();
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

      <div>
        <label htmlFor="accountSelect">Filter by Account: </label>
        <select
          id="account-filter"
          value={selectedAccountId}
          onChange={handleAccountChange}
          disabled={isAccountsLoading}
        >
          <option value="all">All Accounts</option>
          {accounts.map((acc) => (
            <option key={acc.accountId} value={acc.accountId}>
              {acc.name} ({acc.officialName})
            </option>
          ))}
        </select>
      </div>

      <button onClick={() => setShowAddForm(true)}>
        + Add New Subscription
      </button>
      {showAddForm && (
        <SubscriptionForm
          onClose={() => setShowAddForm(false)}
          onSuccess={handleSubscriptionAdded}
        />
      )}

      {editingSubscription && (
        <SubscriptionForm
          onClose={() => setEditingSubscription(null)}
          onSuccess={handleSubscriptionUpdated}
          existingSubscription={editingSubscription}
        />
      )}

      <h2>Spending Distribution</h2>
      <SubscriptionPieChart key={subscriptions.length} />
      <hr />

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
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {subscriptions.length === 0 && (
              <tr>
                <td colSpan={5}>No subscriptions detected.</td>
              </tr>
            )}
            {subscriptions.map((sub) => (
              <tr
                key={sub.subscriptionId}
                style={{ opacity: sub.isActive ? 1 : 0.5 }}
              >
                <td>{sub.merchantName}</td>
                <td>${Math.abs(sub.estimatedAmount).toFixed(2)}</td>
                <td>{sub.frequency}</td>
                <td>{sub.lastPaymentDate}</td>
                <td>{sub.nextDueDate}</td>
                <td>
                  <button onClick={() => setEditingSubscription(sub)}>
                    Edit
                  </button>
                  <button
                    onClick={() => handleDeleteSubscription(sub.subscriptionId)}
                  >
                    Delete
                  </button>
                </td>
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
          {/* {transactions */}
          {filteredTransactions.length === 0 && (
            <tr>
              <td colSpan={5}>No transactions found for selected account.</td>
            </tr>
          )}
          {filteredTransactions.map((t) => (
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
