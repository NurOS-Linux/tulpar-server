public class Config {
    private Server server;

    // Getter and setter for server
    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public static class Server {
        private String address;
        private int port;
        private boolean runInBackground;

        // Getters and setters
        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isRunInBackground() {
            return runInBackground;
        }

        public void setRunInBackground(boolean runInBackground) {
            this.runInBackground = runInBackground;
        }
    }
}
