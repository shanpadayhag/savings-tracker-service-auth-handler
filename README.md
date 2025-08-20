```sh
sam build --use-container
```

```sh
sam deploy --guided --profile dev
sam deploy --profile dev
```

```sh
sam local invoke LoginFunction --event events/login.json
```
