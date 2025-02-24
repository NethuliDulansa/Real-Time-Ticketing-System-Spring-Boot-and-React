# Ticketing System GUI Documentation

## Setup Instructions

### Prerequisites

- [Node.js](https://nodejs.org/) (version 18 or higher)
- [npm](https://www.npmjs.com/) or [yarn](https://yarnpkg.com/)

### Installation

1. Clone the repository:

   ```sh
   git clone <repository-url>
   cd ticketing_system_gui
   ```

2. Install dependencies:

   ```sh
   npm install
   ```

3. Configure environment variables:

   - Create a `.env` file based on `.env.development` or `.env.production`.

### Running the Application

To start the development server:

```sh
npm run dev
```

Access the application at [http://localhost:5173](http://localhost:5173).

## GUI Usage Guidelines

### Main Components

- **OperationControl**: Manage system operations such as start, stop, pause, and resume.
- **ConfigurationManagement**: Update and manage system configurations.
- **CustomerManagement**: Handle customer-related actions and data.
- **VendorManagement**: Add and manage vendor information.
- **TicketSalesOverview**: Visualize ticket sales over time using charts.
- **TransactionLog**: Display real-time transaction logs.

### Theme Toggle

- **ModeToggle**: Switch between light and dark themes.

### Notifications

- **Toaster**: Receive success and error notifications.

## Troubleshooting

### Common Issues

#### Application Fails to Start

- **Check Dependencies**: Ensure all dependencies are installed by running `npm install`.
- **Environment Variables**: Verify that the `.env` file is correctly configured.

#### Getting Help

For further assistance, refer to the project documentation or contact the development team.

## Building the Project

To build the project for production, run:

```sh
npm run build
```

The build output will be located in the `dist` directory.

## Additional Resources

- Vite Configuration: Configuration settings for Vite.
- TypeScript Configurations: TypeScript compiler options.
- PostCSS Configuration: PostCSS setup for processing CSS.

For more detailed information, refer to the respective configuration files in the project structure.

# Acknowledgements

- [React](https://reactjs.org/)
- [Vite](https://vitejs.dev/)
- [Tailwind CSS](https://tailwindcss.com/)
- [TypeScript](https://www.typescriptlang.org/)
