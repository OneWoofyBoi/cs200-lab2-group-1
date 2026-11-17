# Build and run ChocAn

Install a full desktop JDK 17 or newer. Open a terminal in the project folder containing `src`. Check that `java -version` and `javac -version` point to JDK 17 or newer. Run these commands one at a time in Windows Command Prompt, macOS Terminal, or a Linux shell.

```text
javac --release 17 -encoding UTF-8 -sourcepath src/main/java -d build/app src/main/java/chocan/Main.java
java -cp build/app chocan.Main --gui
```

The second command opens the dark sign-in window. For terminal mode, run `java -cp build/app chocan.Main`. Reports and directory exports go to `output` by default. Demo records reset on a launch without a state file.

## Sample accounts

| Role | Account ID | Password |
| --- | --- | --- |
| Provider | `000000100` | |
| Operator | `000000200` | `operator123` |
| Manager | `000000300` | |

Provider and manager use their ID only. Keep the leading zeros.

## Bill a service and reopen saved records

1. Start with a new snapshot filename. Run the following command on both launches. Choose another name if the file already exists.

   ```text
   java -cp build/app chocan.Main --gui build/demo-output --state build/demo-state.bin
   ```

2. Sign in as **Operator**. Open **Members**, select **Add member**, and save ID `000000009`, name `Demo Member`, address `9 Main St`, city `Tuscaloosa`, state `AL`, ZIP `35401`, and status **Active**. The member should appear in the list. To edit or delete an existing record, open **Members** or **Providers**, select the record, and choose **Edit selected** or **Delete selected**.
3. Sign out and sign in as **Provider**. Validate member `000000009` and confirm the result is **Validated**. Open **Record service**, enter the same member ID, keep the current service date, and enter code `598470`. Click **Review service**, verify **Dietitian session** and **$50.00**, then click **Confirm and record**. Open **Provider directory** to show the services and fees.
4. Close the window normally and wait for the process to exit. Run the same launch command again. Use the snapshot from only one running instance at a time. As Operator, open **Members** and find `000000009 | Demo Member | Active`. Sign out and sign in as **Manager**, then generate a **Member report** for `000000009` and confirm that Dietitian session appears. The default report period includes the current receipt date. If the receipt date falls outside the current report period, adjust **Report period** to include it.
5. The Manager workspace also offers a **Provider report** for `000000100`, a **Summary report**, and **Weekly accounting**. The app displays results in the window and writes reports and EFT files to the selected output folder.
