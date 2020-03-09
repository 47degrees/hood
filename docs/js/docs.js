/**
 * Aux function to retrieve repository stars and watchers count info from
 * GitHub API and set it on its proper nodes.
 */
async function loadGitHubStats() {

  const ghAPI = 'https://api.github.com/repos/47deg/hood';
  const ghDataResponse = await fetch(ghAPI);
  const ghData = await ghDataResponse.json();
  const watchersElement = document.querySelector("#eyes");
  const starsElement = document.querySelector("#stars");
  watchersElement.textContent = ghData.subscribers_count;
  starsElement.textContent = ghData.stargazers_count;
}

window.addEventListener("DOMContentLoaded", () => {
  loadGitHubStats();
});
