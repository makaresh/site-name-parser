# Endpoints

| Method | Endpoint                              | Request body | Description                                                                                                                                 |
|--------|---------------------------------------|--------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| Post   | `/api/title/GetSiteName`              | "urls": []   | receiving sequence of urls and for each return extracted page title                                                                         |
| Post   | `/api/title/GetSiteNameAsync`         | "urls": []   | receiving sequence of urls and return id of background task. asynchronously extract page titles and save them                               |
| Get    | `/api/title/?taskId=?&limit=?&offset` |              | returning status of task and extracted page's titles for given task. Array with extracted titles returns only if task successfully finished |

