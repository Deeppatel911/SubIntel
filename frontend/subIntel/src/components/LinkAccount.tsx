import { useState, useEffect } from "react";
import { usePlaidLink } from "react-plaid-link";

const LinkAccount = () => {
  const [linkToken, setLinkToken] = useState<string | null>(null);

  useEffect(() => {
    const createLinkToken = async () => {
      try {
        const response = await fetch(
          "http://localhost:8080/api/plaid/create_link_token",
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
            },
          }
        );

        if (!response.ok) {
          // Log the error status and text for better debugging
          const errorText = await response.text();
          throw new Error(
            `HTTP error! status: ${response.status}, message: ${errorText}`
          );
        }

        const data = await response.json();
        setLinkToken(data.link_token);
      } catch (error) {
        console.error("Error fetching link token:", error);
      }
    };
    createLinkToken();
  }, []);

  const { open, ready } = usePlaidLink({
    token: linkToken,
    onSuccess: (public_token, metadata) => {
      console.log("Plaid link success!", public_token);
      console.log("Metadata:", metadata);
      const exchangeToken = async () => {
        try {
          const jwtToken = localStorage.getItem("jwtToken");
          const response = await fetch(
            "http://localhost:8080/api/plaid/exchange_public_token",
            {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${jwtToken}`,
              },
              body: JSON.stringify({ public_token: public_token }),
            }
          );

          if (response.ok) {
            console.log("Successfully exchanged public token!");
          } else {
            console.error("Failed to exchange public token");
          }
        } catch (error) {
          console.error("Error exchanging token:", error);
        }
      };
      exchangeToken();
    },
  });

  return (
    <button onClick={() => open()} disabled={!ready}>
      Link a Bank Account
    </button>
  );
};

export default LinkAccount;
