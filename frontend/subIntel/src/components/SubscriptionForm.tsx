import React, { useEffect, useState } from "react";
import { Frequency } from "../model/Frequency";

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
    }
  }, [isEditMode, existingSubscription]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
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

  const formStyle: React.CSSProperties = {
    border: "1px solid grey",
    padding: "15px",
    margin: "15px 0",
    borderRadius: "5px",
    backgroundColor: "#f9f9f9", // Light background
  };
  const inputStyle: React.CSSProperties = {
    margin: "5px",
    padding: "8px",
    width: "95%",
  };
  const buttonStyle: React.CSSProperties = {
    margin: "5px",
    padding: "10px 15px",
  };
  const errorStyle: React.CSSProperties = { color: "red", marginTop: "10px" };

  const labelStyle: React.CSSProperties = {
    color: 'black', // Or any dark color
    display: 'block',
    marginBottom: '2px'
  };

  return (
    <div style={formStyle}>
      <h3 style={{'color':'black'}}>{isEditMode ? "Edit Subscription" : "Add New Subscription"}</h3>
      <form onSubmit={handleSubmit}>
        <div>
          <label style={labelStyle}>Merchant Name*:</label>
          <input
            type="text"
            name="merchantName"
            value={formData.merchantName}
            onChange={handleChange}
            style={inputStyle}
            required
          />
        </div>
        <div>
          <label style={labelStyle}>Estimated Amount*:</label>
          <input
            type="number"
            step="0.01"
            name="estimatedAmount"
            value={formData.estimatedAmount}
            onChange={handleChange}
            style={inputStyle}
            required
          />
        </div>
        <div>
          <label style={labelStyle}>Frequency*:</label>
          <select
            name="frequency"
            value={formData.frequency}
            onChange={handleChange}
            style={inputStyle}
            required
          >
            <option value="" disabled>
              Select Frequency
            </option>

            {Object.values(Frequency).map((freq) => (
              <option key={freq} value={freq}>
                {freq}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label style={labelStyle}>Last Payment Date (Optional):</label>
          <input
            type="date"
            name="lastPaymentDate"
            value={formData.lastPaymentDate}
            onChange={handleChange}
            style={inputStyle}
          />
        </div>
        <div>
          <label style={labelStyle}>Next Due Date (Optional):</label>
          <input
            type="date"
            name="nextDueDate"
            value={formData.nextDueDate}
            onChange={handleChange}
            style={inputStyle}
          />
        </div>

        {error && <p style={errorStyle}>{error}</p>}

        <div>
          <button type="submit" disabled={isSubmitting} style={buttonStyle}>
            {isSubmitting
              ? "Saving..."
              : isEditMode
              ? "Update Subscription"
              : "Save Subscription"}
          </button>
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            style={buttonStyle}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};

export default SubscriptionForm;
