import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import LinkAccount from "../components/LinkAccount";
import SubscriptionPieChart from "../components/SubscriptionPieChart";
import SubscriptionForm from "../components/SubscriptionForm";
import {
  Container,
  Box,
  Typography,
  Button,
  Divider,
  Select,
  MenuItem,
  InputLabel,
  FormControl,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Modal,
  Card,
  CardContent,
  CircularProgress,
  AppBar,
  Toolbar,
  CssBaseline,
  type SelectChangeEvent,
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import AddIcon from "@mui/icons-material/Add";
import Grid from "@mui/material/Grid";
import SpendingLineChart from "../components/SpendingLineChart";

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
  itemId: string;
}

const modalStyle = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: 400,
  bgcolor: "background.paper",
  boxShadow: 24,
  p: 4,
  borderRadius: 2,
};

export const Dashboard = () => {
  const navigate = useNavigate();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [isSubLoading, setIsSubLoading] = useState(false);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [selectedAccountId, setSelectedAccountId] = useState<string>("all");
  const [isAccountsLoading, setIsAccountsLoading] = useState(false);

  const [showFormModal, setShowFormModal] = useState(false);
  const [editingSubscription, setEditingSubscription] =
    useState<Subscription | null>(null);
  const [chartVersion, setChartVersion] = useState(0);

  const fetchAccounts = async () => {
    setIsAccountsLoading(true);
    try {
      const jwtToken = localStorage.getItem("jwtToken");
      const response = await fetch("http://localhost:8080/api/accounts", {
        headers: { Authorization: `Bearer ${jwtToken}` },
      });
      if (response.ok) setAccounts(await response.json());
      else setAccounts([]);
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
      if (response.ok) setSubscriptions(await response.json());
      else setSubscriptions([]);
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
      await fetch("http://localhost:8080/api/plaid/transactions", {
        method: "POST",
        headers: { Authorization: `Bearer ${jwtToken}` },
      });
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
      if (transResponse.ok) setTransactions(await transResponse.json());
      await fetchAccounts();
      await fetchSubscriptions().then(() => {
        setChartVersion((v) => v + 1);
      });
    } catch (error) {
      console.error("Error during sync process:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAccountChange = (event: SelectChangeEvent<string>) => {
    setSelectedAccountId(event.target.value as string);
  };

  const handleDeleteSubscription = async (subscriptionId: number) => {
    if (!window.confirm("Are you sure you want to delete this subscription?"))
      return;
    try {
      const jwtToken = localStorage.getItem("jwtToken");
      const response = await fetch(
        `http://localhost:8080/api/subscriptions/${subscriptionId}`,
        { method: "DELETE", headers: { Authorization: `Bearer ${jwtToken}` } }
      );
      if (response.ok) {
        fetchSubscriptions().then(() => {
          setChartVersion((v) => v + 1);
        });
      } else alert("Failed to delete subscription.");
    } catch (error) {
      alert("An error occurred while deleting.");
      console.error("Error deleting subscription:", error);
    }
  };

  const handleFormSuccess = () => {
    setShowFormModal(false);
    setEditingSubscription(null);
    fetchSubscriptions().then(() => {
      setChartVersion((v) => v + 1);
    });
  };

  const handleFormClose = () => {
    setShowFormModal(false);
    setEditingSubscription(null);
  };

  const handleLogout = () => {
    localStorage.removeItem("jwtToken");
    navigate("/login");
  };

  const filteredTransactions = transactions.filter(
    (t) => selectedAccountId === "all" || t.accountId === selectedAccountId
  );

  const handleUnlinkAccount = async (itemId: string, accountName: string) => {
    if (
      !window.confirm(
        `Are you sure you want to unlink "${accountName}"? This will delete all associated accounts and transactions.`
      )
    )
      return;

    try {
      const jwtToken = localStorage.getItem("jwtToken");
      const response = await fetch(
        `http://localhost:8080/api/plaid/item/${itemId}`,
        {
          method: "DELETE",
          headers: { Authorization: `Bearer ${jwtToken}` },
        }
      );

      if (response.ok) {
        setSelectedAccountId("all");
        await fetchAccounts();
        await fetchSubscriptions();

        const transResponse = await fetch(
          "http://localhost:8080/api/transactions",
          {
            headers: { Authorization: `Bearer ${jwtToken}` },
          }
        );
        if (transResponse.ok) setTransactions(await transResponse.json());
        else setTransactions([]);
      } else {
        const errorData = await response.text();
        console.error("Failed to unlink account:", errorData);
        alert(`Failed to unlink: ${errorData}`);
      }
    } catch (error) {
      console.error("Error unlinking account:", error);
      alert("An error occurred while unlinking the account.");
    }
  };

  return (
    <Box sx={{ display: "flex" }}>
      <CssBaseline />

      {/* --- App Bar (Header) --- */}
      <AppBar position="fixed">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            SubIntel
          </Typography>
          <Button color="inherit" onClick={handleLogout}>
            Logout
          </Button>
        </Toolbar>
      </AppBar>
      <Divider sx={{ mb: 3 }} />

      {/* --- Main Content Area --- */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          bgcolor: "background.default",
          p: 3,
          mt: 8, 
        }}
      >
        <Container maxWidth="lg">
          {/* --- Top Control Bar --- */}
          <Card sx={{ mb: 3 }}>
            <CardContent
              sx={{
                display: "flex",
                flexWrap: "wrap",
                gap: 2,
                alignItems: "center",
              }}
            >
              <Grid item>
                {" "}
                {/* Added item prop */}
                <LinkAccount />
              </Grid>
              <Grid item>
                {" "}
                {/* Added item prop */}
                <Button
                  variant="contained"
                  onClick={handleSyncTransactions}
                  disabled={isLoading}
                  startIcon={
                    isLoading ? (
                      <CircularProgress size={20} color="inherit" />
                    ) : null
                  }
                >
                  {isLoading ? "Syncing..." : "Sync Transactions"}
                </Button>
              </Grid>
              <Grid item xs={12} sm={4} sx={{ ml: "auto" }}>
                {/* Added item prop */}
                <FormControl fullWidth size="small">
                  <InputLabel id="account-filter-label">
                    Filter by Account
                  </InputLabel>
                  <Select
                    labelId="account-filter-label"
                    value={selectedAccountId}
                    label="Filter by Account"
                    onChange={handleAccountChange}
                    disabled={isAccountsLoading}
                  >
                    <MenuItem value="all">All Accounts</MenuItem>
                    {accounts.map((acc) => (
                      <MenuItem
                        key={acc.accountId}
                        value={acc.accountId}
                        sx={{ justifyContent: "space-between" }}
                      >
                        <span>
                          {acc.name} ({acc.officialName})
                        </span>
                        <Button
                          size="small"
                          color="error"
                          variant="outlined"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleUnlinkAccount(acc.itemId, acc.name);
                          }}
                          sx={{ ml: 2 }}
                        >
                          Unlink
                        </Button>
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </CardContent>
          </Card>

          {/* --- Main Content Grid --- */}
          <Grid container spacing={3}>
            {/* --- Spending Chart --- */}
            <Grid item xs={12} md={5}>
              <Card sx={{ height: "100%" }}>
                <CardContent
                  sx={{
                    display: "flex",
                    flexDirection: "column",
                    height: "100%",
                  }}
                >
                  <Typography variant="h6" gutterBottom>
                    Subscription Distribution
                  </Typography>
                  <Box
                    sx={{
                      flexGrow: 1,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    <SubscriptionPieChart key={chartVersion} />
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            {/* --- Spending Trend Chart --- */}
            <Grid item xs={12} md={7}>
              <Card sx={{ height: "100%" }}>
                <CardContent
                  sx={{
                    display: "flex",
                    flexDirection: "column",
                    height: "100%",
                  }}
                >
                  <Box sx={{ flexGrow: 1, position: "relative" }}>
                    <SpendingLineChart />
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            {/* --- Subscriptions --- */}
            <Grid item xs={12} md={7}>
              <Card>
                <CardContent>
                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                      mb: 2,
                    }}
                  >
                    <Typography variant="h6">Subscriptions</Typography>
                    <Button
                      variant="contained"
                      startIcon={<AddIcon />}
                      onClick={() => setShowFormModal(true)}
                    >
                      Add New
                    </Button>
                  </Box>
                  <TableContainer component={Paper}>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>Merchant</TableCell>
                          <TableCell align="right">Amount</TableCell>
                          <TableCell>Frequency</TableCell>
                          <TableCell>Last Payment</TableCell>
                          <TableCell>Next Due</TableCell>
                          <TableCell align="center">Actions</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {isSubLoading && (
                          <TableRow>
                            <TableCell colSpan={6} align="center">
                              <CircularProgress size={24} />
                            </TableCell>
                          </TableRow>
                        )}
                        {!isSubLoading && subscriptions.length === 0 && (
                          <TableRow>
                            <TableCell colSpan={6} align="center">
                              No subscriptions detected.
                            </TableCell>
                          </TableRow>
                        )}
                        {!isSubLoading &&
                          subscriptions.map((sub) => (
                            <TableRow
                              key={sub.subscriptionId}
                              sx={{ opacity: sub.isActive ? 1 : 0.5 }}
                            >
                              <TableCell component="th" scope="row">
                                {sub.merchantName}
                              </TableCell>
                              <TableCell align="right">
                                ${Math.abs(sub.estimatedAmount).toFixed(2)}
                              </TableCell>
                              <TableCell>{sub.frequency}</TableCell>
                              <TableCell>{sub.lastPaymentDate}</TableCell>
                              <TableCell>{sub.nextDueDate}</TableCell>
                              <TableCell align="center" sx={{ p: 0 }}>
                                <IconButton
                                  onClick={() => setEditingSubscription(sub)}
                                  size="small"
                                  color="primary"
                                >
                                  <EditIcon />
                                </IconButton>
                                <IconButton
                                  onClick={() =>
                                    handleDeleteSubscription(sub.subscriptionId)
                                  }
                                  size="small"
                                  color="error"
                                >
                                  <DeleteIcon />
                                </IconButton>
                              </TableCell>
                            </TableRow>
                          ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </CardContent>
              </Card>
            </Grid>

            {/* --- Transactions --- */}
            <Grid item xs={12} md={5}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Transactions
                  </Typography>
                  <TableContainer component={Paper} sx={{ maxHeight: 440 }}>
                    <Table stickyHeader size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>Date</TableCell>
                          <TableCell>Name</TableCell>
                          <TableCell>Account</TableCell>
                          <TableCell>Category</TableCell>
                          <TableCell align="right">Amount</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {isLoading && (
                          <TableRow>
                            <TableCell colSpan={5} align="center">
                              <CircularProgress size={24} />
                            </TableCell>
                          </TableRow>
                        )}
                        {!isLoading && filteredTransactions.length === 0 && (
                          <TableRow>
                            <TableCell colSpan={5} align="center">
                              No transactions found for selected account.
                            </TableCell>
                          </TableRow>
                        )}
                        {!isLoading &&
                          filteredTransactions.map((t) => (
                            <TableRow key={t.id}>
                              {" "}
                              {/* Corrected to <TableRow> */}
                              <TableCell>{t.date}</TableCell>
                              <TableCell>{t.name}</TableCell>
                              <TableCell>{t.accountName}</TableCell>
                              <TableCell>{t.category}</TableCell>
                              <TableCell align="right">
                                ${t.amount.toFixed(2)}
                              </TableCell>
                            </TableRow>
                          ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* --- Add/Edit Modal (Consolidated) --- */}
          <Modal
            open={showFormModal || !!editingSubscription}
            onClose={handleFormClose}
          >
            <Box sx={modalStyle}>
              <SubscriptionForm
                onClose={handleFormClose}
                onSuccess={handleFormSuccess}
                existingSubscription={editingSubscription}
              />
            </Box>
          </Modal>
        </Container>
      </Box>
    </Box>
  );
};
