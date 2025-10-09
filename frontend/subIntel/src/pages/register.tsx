import React, {useState} from 'react';
import { useNavigate } from 'react-router-dom';

export const Register = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName:'',
    email: '',
    password: '',
  });
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({...formData, [e.target.name]: e.target.value});
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        navigate('/login');
      }
      else{
        const errorData = await response.text();
        console.error('Registration failed:', errorData);
      }
  }
  catch (error) {
      console.error('Error during registration:', error);
    }
};

  return (
    <div>
      <form onSubmit={handleSubmit}>
            {/* Input fields for firstName, lastName, email, password */}
            {/* Each input should have a name, value, and onChange handler */}
            <input name="firstName" value={formData.firstName} onChange={handleChange} placeholder="First Name" />
            <input name="lastName" value={formData.lastName} onChange={handleChange} placeholder="Last Name" />
            <input name="email" type="email" value={formData.email} onChange={handleChange} placeholder="Email" />
            <input name="password" type="password" value={formData.password} onChange={handleChange} placeholder="Password" />
            <button type="submit">Register</button>
        </form>
    </div>
  )
};
