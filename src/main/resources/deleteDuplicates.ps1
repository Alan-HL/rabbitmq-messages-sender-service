$username = "aalvarado" 
$password = "ofpu**wewp"

$nuxeoUids = @("") #Fill this list with the nuxeo uids that you want to delete

$url = "https://nuxeo-dev.apps-dev.usdc10.axadmin.net/nuxeo/api/v1/id/"

$base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(("{0}:{1}" -f $username,$password)))

foreach ($uid in $nuxeoUids) {
    $newUrl = $url + $uid
    Write-Output $newUrl
    Invoke-RestMethod -Headers @{Authorization=("Basic {0}" -f $base64AuthInfo)} -Method 'Delete' -Uri $newUrl
}