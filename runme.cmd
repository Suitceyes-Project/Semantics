@echo off
cd C:\Users\kass\Desktop\suitceyes\src\main\resources
start powershell -command "$Host.UI.RawUI.WindowTitle = 'ontology_population_and_inference_VA.log'; $pshost = Get-Host ; $psWindow = $pshost.UI.RawUI ;  $newSize = $psWindow.WindowSize ;  $newSize.Height = 27 ; $newSize.Width =172; $psWindow.WindowSize= $newSize ; add-type -AssemblyName microsoft.VisualBasic ; add-type -AssemblyName System.Windows.Forms ; [System.Windows.Forms.SendKeys]::SendWait('~'); get-content C:\Users\kass\Desktop\suitceyes\suitceyes\src\main\resources\logOntologyPopulationAndInferenceVA.log -wait -tail 10"
start powershell -command "$Host.UI.RawUI.WindowTitle = 'ontology_population_and_inference_IB.log'; $pshost = Get-Host ; $psWindow = $pshost.UI.RawUI ;  $newSize = $psWindow.WindowSize ;  $newSize.Height = 27 ; $newSize.Width =172; $psWindow.WindowSize= $newSize ; add-type -AssemblyName microsoft.VisualBasic ; add-type -AssemblyName System.Windows.Forms ; [System.Windows.Forms.SendKeys]::SendWait('~'); get-content C:\Users\kass\Desktop\suitceyes\suitceyes\src\main\resources\logOntologyPopulationAndInferenceIB.log -wait -tail 10"
start powershell -command "$Host.UI.RawUI.WindowTitle = 'messages_arrived.log'; $pshost = Get-Host ; $psWindow = $pshost.UI.RawUI ;  $newSize = $psWindow.WindowSize ;  $newSize.Height = 9 ; $newSize.Width =92; $psWindow.WindowSize= $newSize ; add-type -AssemblyName microsoft.VisualBasic ; add-type -AssemblyName System.Windows.Forms ; [System.Windows.Forms.SendKeys]::SendWait('~'); get-content C:\Users\kass\Desktop\suitceyes\suitceyes\src\main\resources\logMessagesArrived.log -wait -tail 10"
start powershell -command "$Host.UI.RawUI.WindowTitle = 'messages_served.log'; $pshost = Get-Host ; $psWindow = $pshost.UI.RawUI ;  $newSize = $psWindow.WindowSize ;  $newSize.Height = 9 ; $newSize.Width =92; $psWindow.WindowSize= $newSize ; add-type -AssemblyName microsoft.VisualBasic ; add-type -AssemblyName System.Windows.Forms ; [System.Windows.Forms.SendKeys]::SendWait('~'); get-content C:\Users\kass\Desktop\suitceyes\suitceyes\src\main\resources\logMessagesServed.log -wait -tail 10"
start powershell -command "$Host.UI.RawUI.WindowTitle = 'log_if_specific_entity_found.log'; $pshost = Get-Host ; $psWindow = $pshost.UI.RawUI ;  $newSize = $psWindow.WindowSize ;  $newSize.Height = 25 ; $newSize.Width =132; $psWindow.WindowSize= $newSize ; add-type -AssemblyName microsoft.VisualBasic ; add-type -AssemblyName System.Windows.Forms ; [System.Windows.Forms.SendKeys]::SendWait('~'); get-content C:\Users\kass\Desktop\suitceyes\suitceyes\src\main\resources\logIfEntityFound.log -wait -tail 10"
start powershell -command "$Host.UI.RawUI.WindowTitle = 'log_timestamp_difference.log'; $pshost = Get-Host ; $psWindow = $pshost.UI.RawUI ;  $newSize = $psWindow.WindowSize ;  $newSize.Height = 20 ; $newSize.Width =135; $psWindow.WindowSize= $newSize ; add-type -AssemblyName microsoft.VisualBasic ; add-type -AssemblyName System.Windows.Forms ; [System.Windows.Forms.SendKeys]::SendWait('~'); get-content C:\Users\kass\Desktop\suitceyes\suitceyes\src\main\resources\logTimestampDiff.log -wait -tail 10"
exit