package Commons;
import Commands.*;
import DukeExceptions.DukeException;
import UserInterface.AlertBox;
import Tasks.*;
import javafx.scene.control.Alert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deals with the input of the user and tries to understand the
 * user's input with fixed commands.
 */
public class Parser {

    private static String[] split;
    private static String[] split1;
    private static String[] split2;
    private static String[] split3;
    private static String[] split4;
    private static LookupTable LT = new LookupTable();
    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());

    /**
     * This method breaks apart the user's input and tries to make sense with it.
     * @param fullCommand The user's input
     * @return This returns a Command object based on user's input
     * @throws DukeException On invalid input or when wrong input format is entered
     */
    public static Command parse(String fullCommand) throws DukeException, ParseException {
        try {
            if (fullCommand.trim().equals("bye")) {
                return new ByeCommand();
            }else if(fullCommand.trim().equalsIgnoreCase("help")){
                return new HelpCommand();
            } else if (fullCommand.trim().contains("show previous")) {
                return new ShowPreviousCommand(fullCommand);
            } else if (fullCommand.trim().contains("retrieve previous")) {
                return new RetrievePreviousCommand(fullCommand);
            } else if (fullCommand.trim().substring(0, 4).equals("list")) {
                try {
                    String list = fullCommand.trim().substring(5);
                    if (list.trim().isEmpty()) {
                        throw new DukeException("\u2639" + " OOPS!!! Please do not leave name of list blank.");
                    } else if (list.trim().equals("todo")) {
                        return new ListCommand(list);
                    } else if (list.trim().equals("event")) {
                        return new ListCommand(list);
                    } else if (list.trim().equals("deadline")) {
                        return new ListCommand(list);
                    } else {
                        throw new DukeException("\u2639" + " OOPS!!! Please enter name of list");
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    LOGGER.log(Level.INFO, e.toString(), e);
                    throw new DukeException("OOPS!!! Please enter list as follows:\n" +
                            "list name_of_list_to_view\n" +
                            "For example: list todo");
                }
            } else if(fullCommand.startsWith("Week")) {
                String week = fullCommand.replaceFirst("Week", "");
                week.trim();
//                try{
//                    Integer digit = Integer.parseInt(week);
//                    if(digit < 1 || digit > 13 ) throw new DukeException("Invalid week command\n" + "Format: Week 'x', where 'x' is a digit." );
//                } catch (NumberFormatException e) {
//                    throw new DukeException("Invalid week command\n" + "Format: Week 'x', where 'x' is a digit." );
//                }
                return new WeekCommand(fullCommand.trim());
            } else if(fullCommand.trim().startsWith("done/e")){
                try { //add/e module_code description /at date from time to time
                    String activity = fullCommand.replaceFirst("done/e", "");
                    split = activity.split("/at"); //split[0] is " module_code description", split[1] is "date from time to time"
                    if (split[0].trim().isEmpty()) {
                        throw new DukeException("\u2639" + " OOPS!!! The description of a event cannot be empty.");
                    }
                    split1 = split[1].split("/from"); //split1[0] is "date", split1[1] is "time to time"
                    String weekDate;
                    split2 = split1[0].trim().split(" ");
                    weekDate = split2[0];
                    if(weekDate.equalsIgnoreCase("reading") || weekDate.equalsIgnoreCase("exam")
                            || weekDate.equalsIgnoreCase("week") || weekDate.equalsIgnoreCase("recess")){
                        weekDate = LT.getValue(split1[0].trim());
                    }else{
                        weekDate = split1[0].trim();
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); //format date
                    Date date = formatter.parse(weekDate.trim());
                    split2 = split1[1].split("/to"); //split2[0] is (start) "time", split2[1] is (end) "time"
                    SimpleDateFormat formatter1 = new SimpleDateFormat("HHmm"); //format time
                    Date startTime = formatter1.parse(split2[0].trim());
                    Date endTime = formatter1.parse(split2[1].trim());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("E dd/MM/yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                    String dateString = dateFormat.format(date);
                    String startTimeString = timeFormat.format(startTime);
                    String endTimeString = timeFormat.format(endTime);
                    return new DoneCommand("event",new Event(split[0].trim(), dateString, startTimeString, endTimeString));
                } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                    LOGGER.log(Level.INFO, e.toString(), e);
                    throw new DukeException("OOPS!!! Please enter in the format as follows:\n" +
                            "done/e mod_code name_of_event /at dd/MM/yyyy /from HHmm /to HHmm\n" +
                            "or done/e mod_code name_of_event /at week x day /from HHmm /to HHmm\n");
                }
            }  else if (fullCommand.trim().startsWith("done/d")) {
                try {
                    String activity = fullCommand.trim().replaceFirst("done/d", "");
                    split = activity.split("/by");
                    if (split[0].trim().isEmpty()) {
                        throw new DukeException("\u2639" + " OOPS!!! The description of a deadline cannot be empty.");
                    }
                    String weekDate ="";
                    split2 = split[1].trim().split(" ");
                    weekDate = split2[0];
                    if(weekDate.equalsIgnoreCase("reading") || weekDate.equalsIgnoreCase("exam")
                            || weekDate.equalsIgnoreCase("week") || weekDate.equalsIgnoreCase("recess")){
                        weekDate = split[1].substring(0,split[1].length()- 4); // week x day y
                        String time = split[1].substring(split[1].length()- 4); // time E.g 0300
                        weekDate = LT.getValue(weekDate) + " " + time;
                    }else{
                        weekDate = split[1];
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HHmm");
                    Date date = formatter.parse(weekDate);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("E dd/MM/yyyy");
                    String dateString = dateFormat.format(date);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                    String timeString = timeFormat.format(date);
                    return new DoneCommand("deadline",new Deadline(split[0].trim(), dateString, timeString));

                } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                    LOGGER.log(Level.INFO, e.toString(), e);
                    throw new DukeException("OOPS!!! Please enter in the format as follows:\n" +
                            "done/d mod_code name_of_event /by dd/MM/yyyy HHmm\n" +
                            "or done/d mod_code name_of_event /by week x day HHmm\n");
                }
            } else if (fullCommand.trim().substring(0, 5).equals("add/e")) {
                try { //add/e module_code description /at date /from time /to time
                    String activity = fullCommand.trim().substring(5);
                    split = activity.split("/at"); //split[0] is " module_code description", split[1] is "date /from time /to time"
                    if (split[0].trim().isEmpty()) {
                        throw new DukeException("\u2639" + " OOPS!!! The description of a event cannot be empty.");
                    }
                    split1 = split[1].split("/from"); //split1[0] is "date", split1[1] is "time to time"
                    String weekDate ="";
                    split2 = split1[0].trim().split(" ");
                    weekDate = split2[0];
                    if(weekDate.equalsIgnoreCase("reading") || weekDate.equalsIgnoreCase("exam")
                            || weekDate.equalsIgnoreCase("week") || weekDate.equalsIgnoreCase("recess")){
                        weekDate = LT.getValue(split1[0].trim());
                    }else{
                        weekDate = split1[0].trim();
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); //format date
                    Date date = formatter.parse(weekDate.trim());
                    split2 = split1[1].split("/to"); //split2[0] is (start) "time", split2[1] is (end) "time"
                    SimpleDateFormat formatter1 = new SimpleDateFormat("HHmm"); //format time
                    Date startTime = formatter1.parse(split2[0].trim());
                    Date endTime = formatter1.parse(split2[1].trim());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("E dd/MM/yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                    String dateString = dateFormat.format(date);
                    String startTimeString = timeFormat.format(startTime);
                    String endTimeString = timeFormat.format(endTime);
                    return new AddCommand(new Event(split[0].trim(), dateString, startTimeString, endTimeString));
                } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                    LOGGER.log(Level.INFO, e.toString(), e);
                    throw new DukeException("OOPS!!! Please enter event as follows:\n" +
                            "add/e modCode name_of_event /at dd/MM/yyyy from HHmm to HHmm\n" +
                            "For example: add/e CS1231 project meeting /at 1/1/2020 /from 1500 /to 1700");
                }
            } else if (fullCommand.trim().substring(0,7).equals("recur/e")) {
                try {
                    String activity = fullCommand.trim().substring(7);
                    String startWeekDate;
                    String endWeekDate;
                    split = activity.split("/start"); //split[0] is " module_code description", split[1] is "date to date from time to time"
                    if (split[0].trim().isEmpty()) {
                        throw new DukeException("\u2639" + " OOPS!!! The description of a event cannot be empty.");
                    }
                    split1 = split[1].split("/from"); //split1[0] is "date to date" or "week X mon to week X mon", split1[1] is "time to time"
                    split3 = split1[0].split("/to"); //split3[0] is (start) "date", split3[1] is (end) "date"
                    split4 = split3[0].trim().split(" "); //split the start date
                    //recess week mon / week 3 mon / exam week mon / reading week tue
                    startWeekDate = split4[0].trim();
                    if (startWeekDate.equalsIgnoreCase("reading") || startWeekDate.equalsIgnoreCase("exam")
                            || startWeekDate.equalsIgnoreCase("week") || startWeekDate.equalsIgnoreCase("recess")) {
                        startWeekDate = LT.getValue(split3[0].trim());
                    } else {
                        startWeekDate = split3[0].trim();
                    }
                    split4 = split3[1].trim().split(" "); //split the end date
                    endWeekDate = split4[0].trim();
                    if (endWeekDate.equalsIgnoreCase("reading") || endWeekDate.equalsIgnoreCase("exam")
                            || endWeekDate.equalsIgnoreCase("week") || endWeekDate.equalsIgnoreCase("recess")) {
                        endWeekDate = LT.getValue(split3[1].trim());
                    } else {
                        endWeekDate = split3[1].trim();
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); //format date
                    Date startDate = formatter.parse(startWeekDate);
                    Date endDate = formatter.parse(endWeekDate);
                    split2 = split1[1].split("/to"); //split2[0] is (start) "time", split2[1] is (end) "time"
                    SimpleDateFormat formatter1 = new SimpleDateFormat("HHmm"); //format time
                    Date startTime = formatter1.parse(split2[0].trim());
                    Date endTime = formatter1.parse(split2[1].trim());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                    String startTimeString = timeFormat.format(startTime);
                    String endTimeString = timeFormat.format(endTime);
                    return new HelpCommand();
                    // return new RecurringCommand(split[0].trim(),startDate, endDate, startTimeString, endTimeString);
                } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                    LOGGER.log(Level.INFO, e.toString(), e);
                    throw new DukeException("OOPS!!! Please enter recurring event as follows:\n" +
                            "recur/e modCode name_of_event /start dd/MM/yyyy to dd/MM/yyyy /from HHmm /to HHmm\n" +
                            "For example: recur/e CS1231 project meeting /start 1/10/2019 to 15/11/2019 /from 1500 /to 1700");
                }
            }else if(fullCommand.trim().substring(0,8).equals("delete/e")){
                try { //add/e module_code description /at date from time to time
                    String activity = fullCommand.trim().substring(8);
                    split = activity.split("/at"); //split[0] is " module_code description", split[1] is "date from time to time"
                    if (split[0].trim().isEmpty()) {
                        throw new DukeException("\u2639" + " OOPS!!! The description of a event cannot be empty.");
                    }
                    split1 = split[1].split("/from"); //split1[0] is "date", split1[1] is "time to time"
                    String weekDate ="";
                    split2 = split1[0].trim().split(" ");
                    weekDate = split2[0];
                    if(weekDate.equalsIgnoreCase("reading") || weekDate.equalsIgnoreCase("exam")
                            || weekDate.equalsIgnoreCase("week") || weekDate.equalsIgnoreCase("recess")){
                        weekDate = LT.getValue(split1[0].trim());
                    }else{
                        weekDate = split1[0].trim();
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); //format date
                    Date date = formatter.parse(weekDate.trim());
                    split2 = split1[1].split("/to"); //split2[0] is (start) "time", split2[1] is (end) "time"
                    SimpleDateFormat formatter1 = new SimpleDateFormat("HHmm"); //format time
                    Date startTime = formatter1.parse(split2[0].trim());
                    Date endTime = formatter1.parse(split2[1].trim());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("E dd/MM/yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                    String dateString = dateFormat.format(date);
                    String startTimeString = timeFormat.format(startTime);
                    String endTimeString = timeFormat.format(endTime);
                    return new DeleteCommand("event",new Event(split[0].trim(), dateString, startTimeString, endTimeString));
                } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                    LOGGER.log(Level.INFO, e.toString(), e);
                    throw new DukeException("OOPS!!! Please enter in the format as follows:\n" +
                            "delete/e mod_code name_of_event /at dd/MM/yyyy /from HHmm /to HHmm\n" +
                            "or delete/e mod_code name_of_event /at week x day /from HHmm /to HHmm\n");
                }
            } else if (fullCommand.trim().substring(0,6).equals("remind")) {
                try {
                    boolean set = false;
                    String description = "";
                    String activity = fullCommand.trim().substring(6);
                    split = activity.trim().split("/by");
                    if(split[0].contains("/set")){
                        description = split[0].substring(4).trim();
                        if (description.isEmpty()) {
                            throw new DukeException("\u2639" + " OOPS!!! The description of a deadline cannot be empty.");
                        }
                        set = true;
                    } else {
                        description = split[0].substring(3).trim();
                        if (description.isEmpty()) {
                            throw new DukeException("\u2639" + " OOPS!!! The description of a deadline cannot be empty.");
                        }
                    }
                    split1 = split[1].trim().split(" /to ");
                    String weekDate = "";
                    String reminderDate = "";
                    split2 = split1[0].trim().split(" ");
                    weekDate = split2[0];
                    split3 = split1[1].trim().split(" ");
                    reminderDate = split3[0];
                    if (weekDate.equalsIgnoreCase("reading") || weekDate.equalsIgnoreCase("exam")
                            || weekDate.equalsIgnoreCase("week") || weekDate.equalsIgnoreCase("recess")
                            || reminderDate.equalsIgnoreCase("reading") || reminderDate.equalsIgnoreCase("exam")
                            || reminderDate.equalsIgnoreCase("week") || reminderDate.equalsIgnoreCase("recess")) {
                        weekDate = split1[0].substring(0,split1[0].length()- 4);
                        reminderDate = split1[1].substring(0,split1[1].length()- 4);
                        String time = split1[0].substring(split1[0].length()- 4);
                        weekDate = LT.getValue(weekDate) + " " + time;
                        time = split1[1].substring(split1[1].length()- 4);
                        reminderDate = LT.getValue(reminderDate) + " " + time;
                    } else {
                        weekDate = split1[0];
                        reminderDate = split1[1];
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HHmm");
                    Date dateOfTask = formatter.parse(weekDate);
                    Date dateOfReminder = formatter.parse(reminderDate);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("E dd/MM/yyyy");
                    String dateString = dateFormat.format(dateOfTask);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                    String timeString = timeFormat.format(dateOfTask);
                    return new RemindCommand(new Deadline(description, dateString, timeString), dateOfReminder, set);
                } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                    LOGGER.log(Level.INFO, e.toString(), e);
                    throw new DukeException("OOPS!!! Please enter remind as follows:\n" +
                            "remind/(set/rm) mod_code description /by week n.o day time /to week n.o day time\n" +
                            "For example: remind/set cs2100 hand in homework /by week 9 fri 1500 /to week 9 thu 1500");
                }
            } else if (fullCommand.trim().equals("/show workload")) {
                try {
                    Date today = Calendar.getInstance().getTime();
                    Date nextWeek = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String nextWeekDate = formatter.format(nextWeek);
                    return new ShowWorkloadCommand(nextWeekDate);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new DukeException("OOPS!!! Please enter show workload as follows:\n" +
                            "/show workload");
                }
            } else if (fullCommand.trim().substring(0,8).equals("delete/d")) {
                try {
                    String activity = fullCommand.trim().substring(8);
                    split = activity.split("/by");
                    if (split[0].trim().isEmpty()) {
                        throw new DukeException("\u2639" + " OOPS!!! The description of a deadline cannot be empty.");
                    }
                    String weekDate ="";
                    split2 = split[1].trim().split(" ");
                    weekDate = split2[0];
                    if(weekDate.equalsIgnoreCase("reading") || weekDate.equalsIgnoreCase("exam")
                            || weekDate.equalsIgnoreCase("week") || weekDate.equalsIgnoreCase("recess")){
                        weekDate = split[1].substring(0,split[1].length()- 4); // week x day y
                        String time = split[1].substring(split[1].length()- 4); // time E.g 0300
                        weekDate = LT.getValue(weekDate) + " " + time;
                    }else{
                        weekDate = split[1];
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HHmm");
                    Date date = formatter.parse(weekDate);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("E dd/MM/yyyy");
                    String dateString = dateFormat.format(date);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                    String timeString = timeFormat.format(date);
                    return new DeleteCommand("deadline",new Deadline(split[0].trim(), dateString, timeString));

                } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                    LOGGER.log(Level.INFO, e.toString(), e);
                    throw new DukeException("OOPS!!! Please enter in the format as follows:\n" +
                            "delete/d mod_code name_of_event /by dd/MM/yyyy HHmm\n" +
                            "or delete/d mod_code name_of_event /by week x day HHmm\n");
                }
            } else if (fullCommand.trim().substring(0, 5).equals("add/d")) {//deadline
                try {
                    String activity = fullCommand.trim().substring(5);
                    split = activity.split("/by");
                    if (split[0].trim().isEmpty()) {
                        throw new DukeException("\u2639" + " OOPS!!! The description of a deadline cannot be empty.");
                    }
                    String weekDate ="";
                    split2 = split[1].trim().split(" "); //date time
                    weekDate = split2[0];
                    if(weekDate.equalsIgnoreCase("reading") || weekDate.equalsIgnoreCase("exam")
                            || weekDate.equalsIgnoreCase("week") || weekDate.equalsIgnoreCase("recess")){
                        weekDate = split[1].substring(0,split[1].length()- 4); // week x day y
                        String time = split[1].substring(split[1].length()- 4); // time E.g 0300
                        weekDate = LT.getValue(weekDate) + " " + time;
                    }else{
                        weekDate = split[1];
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HHmm");
                    Date date = formatter.parse(weekDate);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("E dd/MM/yyyy");
                    String dateString = dateFormat.format(date);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                    String timeString = timeFormat.format(date);
                    return new AddCommand(new Deadline(split[0].trim(), dateString, timeString));
                } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                    LOGGER.log(Level.INFO, e.toString(), e);
                    throw new DukeException(" OOPS!!! Please enter deadline as follows:\n" +
                            "add/d mod_code name_of_event /by dd/MM/yyyy HHmm\n" +
                            "or add/d mod_code name_of_event /by week x day HHmm\n");
                }
            } else if(fullCommand.trim().substring(0,6).equalsIgnoreCase("filter")){
                String keyword = "";
                keyword = fullCommand.trim().substring(7);
                return new FilterCommand(keyword);
            } else if (fullCommand.contains("(from") && fullCommand.contains("to")) {
                try {
                    boolean isValid = true;
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = new Date();
                    String currentDate = formatter.format(date);
                    int index = fullCommand.indexOf("(from");
                    String taskDescription = fullCommand.substring(0, index);
                    fullCommand = fullCommand.replace(taskDescription, "");
                    fullCommand = fullCommand.replace("(from", "").trim();
                    String[] startAndEndDate = fullCommand.split(" to ", 2);
                    String startDate = startAndEndDate[0];
                    String endDate = startAndEndDate[1].replace(")", "").trim();
                    Date beginDate = new SimpleDateFormat("dd/MM/yyyy").parse(startDate);
                    Date newCurrentDate = new SimpleDateFormat("dd/MM/yyyy").parse(currentDate);

                    if (beginDate.compareTo(newCurrentDate) < 0) { //date is wrong
                        AlertBox.display("Warning message", "Invalid date", "Please enter another valid date",
                                Alert.AlertType.WARNING);
                        isValid = false;
                    }
                    System.out.println("value of isValid: " + isValid);
                    System.out.println("start date: " + startDate + " Current date: " + currentDate);
                    return new DoWithinPeriodTasksCommand(taskDescription, startDate, endDate, isValid);
                } catch (ParseException | ArrayIndexOutOfBoundsException e) {
                    LOGGER.log(Level.INFO, e.toString(), e);
                    throw new DukeException(" OOPS!!! Please enter Do Within Period Task as follows:\n" +
                            " 'Task Description' '(from DD/MM/yyyy to DD/MM/yyyy)'");
                }
            } else if(fullCommand.startsWith("find")) {
                fullCommand = fullCommand.replaceFirst("find", "");
                fullCommand = fullCommand.trim();
                fullCommand = fullCommand.replaceFirst("hours", "");
                fullCommand = fullCommand.trim();
                return new FindFreeTimesCommand(Integer.parseInt(fullCommand));
            } else if (fullCommand.trim().startsWith("retrieve free time ")) {
                fullCommand = fullCommand.replaceFirst("retrieve free time ", "");
                fullCommand = fullCommand.trim();
                return new RetrieveFreeTimesCommand(fullCommand);
            } else {
                throw new DukeException("\u2639" + " OOPS!!! I'm sorry, but I don't know what that means :-(");
            }

        } catch (StringIndexOutOfBoundsException e) {
            LOGGER.log(Level.INFO, e.toString(), e);
            throw new DukeException("\u2639" + " OOPS!!! I'm sorry, but I don't know what that means :-(");
        }
    }
}