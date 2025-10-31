import React, { useEffect, useState } from "react";
import { Frequency } from "../model/Frequency";
import {
  Box,
  Typography,
  Button,
  MenuItem,
  FormControl,
  FormHelperText,
} from "@mui/material";

import TextField from "@mui/material/TextField";
import InputLabel from "@mui/material/InputLabel";
import Select from "@mui/material/Select";
import type { SelectChangeEvent } from "@mui/material/Select";

interface Subscription {
  subscriptionId: number;
  merchantName: string;
  estimatedAmount: number;
  frequency: string;
  nextDueDate: string;
  lastPaymentDate: string;
  isActive: boolean;
}

interface SubscriptionFormProps {
  onClose: () => void;
  onSuccess: () => void;
  existingSubscription?: Subscription | null;
}

interface FormData {
  merchantName: string;
  estimatedAmount: string;
  frequency: Frequency | "";
  lastPaymentDate?: string;
  nextDueDate?: string;
}

const SubscriptionForm: React.FC<SubscriptionFormProps> = ({
  onClose,
  onSuccess,
  existingSubscription,
}) => {
  const isEditMode = !!existingSubscription;
  const [formData, setFormData] = useState<FormData>({
    merchantName: "",
    estimatedAmount: "",
    frequency: "",
    lastPaymentDate: "",
    nextDueDate: "",
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isEditMode && existingSubscription) {
      setFormData({
        merchantName: existingSubscription.merchantName,
        estimatedAmount: String(Math.abs(existingSubscription.estimatedAmount)),
        frequency: existingSubscription.frequency as Frequency,
        lastPaymentDate: existingSubscription.lastPaymentDate || "",
        nextDueDate: existingSubscription.nextDueDate || "",
      });
    } else {
      setFormData({
        merchantName: "",
        estimatedAmount: "",
        frequency: "",
        lastPaymentDate: "",
        nextDueDate: "",
      });
    }
  }, [isEditMode, existingSubscription]);

  const handleChange = (
    e:
      | React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
      | SelectChangeEvent<string>
  ) => {
    const name = (e.target as HTMLInputElement).name;
    const value = e.target.value;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);

    if (
      !formData.merchantName ||
      !formData.estimatedAmount ||
      !formData.frequency
    ) {
      setError("Merchant Name, Amount, and Frequency are required.");
      setIsSubmitting(false);
      return;
    }
    const amount = parseFloat(formData.estimatedAmount);
    if (isNaN(amount)) {
      setError("Estimated Amount must be a valid number.");
      setIsSubmitting(false);
      return;
    }

    const payload = {
      merchantName: formData.merchantName,
      estimatedAmount: amount,
      frequency: formData.frequency,
      lastPaymentDate: formData.lastPaymentDate || null,
      nextDueDate: formData.nextDueDate || null,
    };

    try {
      const jwtToken = localStorage.getItem("jwtToken");
      const url = isEditMode
        ? `http://localhost:8080/api/subscriptions/${existingSubscription?.subscriptionId}`
        : "http://localhost:8080/api/subscriptions";
      const method = isEditMode ? "PUT" : "POST";

      const response = await fetch(url, {
        method: method,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${jwtToken}`,
        },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        onSuccess();
      } else {
        const errorData = await response.text();
        setError(
          `Failed to ${isEditMode ? "update" : "add"} subscription: ${
            errorData || response.statusText
          }`
        );
      }
    } catch (err) {
      setError(
        `An error occurred while ${
          isEditMode ? "updating" : "submitting"
        } the form.`
      );
      console.error(err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box>
      <Typography variant="h6" component="h2" sx={{ mb: 2 }}>
        {isEditMode ? "Edit Subscription" : "Add New Subscription"}
      </Typography>
      <form onSubmit={handleSubmit}>
        <TextField
          label="Merchant Name"
          name="merchantName"
          value={formData.merchantName}
          onChange={handleChange}
          required
          fullWidth
          margin="normal"
        />
        <TextField
          label="Estimated Amount"
          name="estimatedAmount"
          type="number"
          value={formData.estimatedAmount}
          onChange={handleChange}
          required
          fullWidth
          margin="normal"
        />
        <FormControl fullWidth margin="normal" required>
          <InputLabel id="frequency-label">Frequency</InputLabel>
          <Select
            labelId="frequency-label"
            name="frequency"
            value={formData.frequency}
            label="Frequency"
            onChange={handleChange}
          >
            <MenuItem value="" disabled>
              Select Frequency
            </MenuItem>
            {Object.values(Frequency).map((freq) => (
              <MenuItem key={freq} value={freq}>
                {freq}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <TextField
          label="Last Payment Date (Optional)"
          name="lastPaymentDate"
          type="date"
          value={formData.lastPaymentDate}
          onChange={handleChange}
          fullWidth
          margin="normal"
          slotProps={{
            inputLabel: { shrink: true },
          }}
        />
        <TextField
          label="Next Due Date (Optional)"
          name="nextDueDate"
          type="date"
          value={formData.nextDueDate}
          onChange={handleChange}
          fullWidth
          margin="normal"
          slotProps={{
            inputLabel: { shrink: true },
          }}
        />

        {error && (
          <FormHelperText error sx={{ mt: 2 }}>
            {error}
          </FormHelperText>
        )}

        <Box sx={{ mt: 3, display: "flex", justifyContent: "flex-end" }}>
          <Button onClick={onClose} disabled={isSubmitting} sx={{ mr: 1 }}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={isSubmitting}>
            {isSubmitting ? "Saving..." : isEditMode ? "Update" : "Save"}
          </Button>
        </Box>
      </form>
    </Box>
  );
};

export default SubscriptionForm;
