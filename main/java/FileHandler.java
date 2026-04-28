import java.io.*;

public class FileHandler {

    // Load a TaskDataset from a file
    public TaskDataset load(File file) {
        if (file == null)
            throw new IllegalArgumentException("file is null");

        if (!file.exists())
            throw new IllegalArgumentException("file not found: " + file.getAbsolutePath());

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String header = nextNonEmptyLine(br);
            if (header == null)
                throw new IllegalArgumentException("Empty file");

            String[] h = splitLine(header);
            if (h.length < 2)
                throw new IllegalArgumentException("Header must be: N,totalHours");

            int n = Integer.parseInt(h[0].trim());
            double totalHours = Double.parseDouble(h[1].trim());
            int totalUnits = toUnits(totalHours);

            MyArrayList<Task> tasks = new MyArrayList<>();
            String line;
            int count = 0;

            // Read up to N task lines
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = splitLine(line);
                if (parts.length < 4) {
                    throw new IllegalArgumentException("Bad task line: " + line);
                }

                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                if (name.isEmpty())
                    throw new IllegalArgumentException("Task name is empty");
                if (name.contains(","))
                    throw new IllegalArgumentException("Task name must not contain a comma");

                double hours = Double.parseDouble(parts[2].trim());
                validateHours(hours);

                int value = Integer.parseInt(parts[3].trim());

                tasks.add(new Task(id, name, hours, value));
                count++;
            }

            return new TaskDataset(totalUnits, tasks);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }

    // Save a TaskDataset to a file
    public void save(File file, TaskDataset dataset) {
        if (file == null) throw new
                IllegalArgumentException("file is null");

        if (dataset == null) throw new
                IllegalArgumentException("dataset is null");

        // Write to file
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            int n = dataset.getTasks().size();
            out.println(n + "," + dataset.getTotalHoursAsDouble());

            for (int i = 0; i < n; i++) {
                Task t = dataset.getTasks().get(i);
                validateHours(t.getHours());
                if (t.getName() == null || t.getName().trim().isEmpty()) {
                    throw new IllegalArgumentException("Task name is empty for id=" + t.getId());
                }
                if (t.getName().contains(",")) {
                    throw new IllegalArgumentException("Task name must not contain a comma: id=" + t.getId());
                }
                out.println(t.getId() + "," + t.getName().trim() + "," + t.getHours() + "," + t.getValue());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + e.getMessage(), e);
        }
    }

    // Methods to convert between hours and units (0.5 hour = 1 unit)
    public static int toUnits(double hours) {
        return (int) Math.round(hours * 2.0);
    }

    // Method to convert units back to hours
    public static double fromUnits(int units) {
        return units / 2.0;
    }

    // Method to validate that hours is one of the allowed values
    private static void validateHours(double hours) {
        if (hours < 0.5 || hours > 24.0) {
            throw new IllegalArgumentException("Hours must be between 0.5 and 24.0 (inclusive).");
        }
        // must be in 0.5 steps
        double scaled = hours * 2.0;
        long rounded = Math.round(scaled);
        if (Math.abs(scaled - rounded) > 1e-9) {  // rejects 0. somthing other than .0 or .5
            throw new IllegalArgumentException("Hours must be in steps of 0.5 (0.5, 1.0, 1.5, ... 24.0).");
        }
    }

    // Method to read the next non-empty line from BufferedReader
    private static String nextNonEmptyLine(BufferedReader br) throws IOException {
        String s;
        while ((s = br.readLine()) != null) {
            s = s.trim();
            if (!s.isEmpty()) return s;
        }
        return null;
    }

    // Method to split a line
    private static String[] splitLine(String line) {
        String[] raw = line.split(",");
        for (int i = 0; i < raw.length; i++) raw[i] = raw[i].trim();
        return raw;
    }

}
