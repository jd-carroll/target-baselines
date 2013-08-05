Eclipse PDE Target API Baselines
================
The Target Baselines feature allows you to use predefined target definitions as an API Baseline.  The major advantage of this tool over the traditional API Baselines, is that you are able to define a p2 update-site as your API Baseline.  The tool is able to reload the baseline from the update-site without the developer having to download the individual .jar's.

Please see the instructions under <a href="#updatesite">Update Site</a> for installation.

## Usage Instructions
Once the software is <a href="#Update">installed</a>, the following steps outline how to use a target definition as an API Baseline

1. Open your Eclipse IDE
2. Window -> Preferences
3. Navigate to Plug-in Development -> Mansfield Technology -> Target Baselines

The provided preference page behaves the same as the API Baseline and Target Platform preference pages. Thus if you create, edit, or delete a target definition from the Target Baselines page, the Target Platform page will reflect those changes (so long as you applied those changes). Creating a new Target Baseline from the Target Baselines page, creates a new workspace target definition over a traditional API Baseline.

## Software Requirements
The minimum supported version of Eclipse is 4.2.0 (Juno)

## Known Issues

1. Once an API Baseline is created from a Target Definition, the baseline is never removed
2. Exisiting API Baselines do not reload

## Reporting Issues
To report new issues with this feature, please use the <a href="https://github.com/jd-carroll/target-baselines/issues">Issues</a> tab.

## Update Site
<pre>https://raw.github.com/jd-carroll/target-baselines/master/com.mansfield.pde.api.tools.downloads</pre>
* [Downloadable Update Site] (https://github.com/jd-carroll/target-baselines/blob/master/com.mansfield.pde.api.tools.downloads/update-site.zip?raw=true)

To install the software:

1. Open your Eclipse IDE
2. Help -> Install New Software
3. Enter the site:
<pre>https://raw.github.com/jd-carroll/target-baselines/master/com.mansfield.pde.api.tools.downloads</pre>
4. Select PDE API Tools and click finish

Note: I am in the process of getting an SSL Certificate. In the mean time, I am unable to sign the jars and you will be prompted with a warning during the installation process.

## License
This software is distributed under the [EPL](http://www.eclipse.org/legal/epl-v10.html).
