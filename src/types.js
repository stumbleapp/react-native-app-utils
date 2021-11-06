type ImageSource<Object> = {
    uri: string
};

export type RemoteAction<Object> = {
    id: string,
    icon: string | ImageSource,
    title: string,
    desc: string,
    callback: Function
};

export type Actions<RemoteAction>;