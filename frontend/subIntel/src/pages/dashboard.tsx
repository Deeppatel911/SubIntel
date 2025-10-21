import { useState } from 'react';
import LinkAccount from '../components/LinkAccount';

interface Transaction {
  id: number;
  name: string;
  amount: number;
  date: string;
  category: string;
  accountName: string;
}

export const Dashboard = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const handleSyncTransactions = async () => {
      setIsLoading(true);
      const jwtToken = localStorage.getItem('jwtToken');
      
      // Call the backend to start the sync
      await fetch('http://localhost:8080/api/plaid/transactions', {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${jwtToken}` },
      });

      // After sync, fetch the transactions to display them
      const response = await fetch('http://localhost:8080/api/transactions', { // We'll create this GET endpoint next
           headers: { 'Authorization': `Bearer ${jwtToken}` },
      });
      const data = await response.json();
      setTransactions(data);
      setIsLoading(false);
  };

  return (
      <div>
          <h1>Dashboard</h1>
          <LinkAccount />
          <hr />
          <button onClick={handleSyncTransactions} disabled={isLoading}>
              {isLoading ? 'Syncing...' : 'Sync Transactions'}
          </button>

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
                  {transactions.map(t => (
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
