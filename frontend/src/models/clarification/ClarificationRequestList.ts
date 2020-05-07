import ClarificationRequest from './ClarificationRequest';

export default class ClarificationRequestList {
	private _requests!: ClarificationRequest[];
	private _nameMap!: Map<number, String>;
	private _usernameMap!: Map<number, String>;

	constructor(jsonObj: any) {
		this._requests = jsonObj.requests;
		this._nameMap = jsonObj.names;
		this._usernameMap = jsonObj.usernames;

		if (!this._requests || !this._nameMap || !this._usernameMap)
			throw new Error('invalid ClarificationRequestList response from server');
	}

	get requests() {
		return this._requests;
	}

	nameForUser(id: number): String {
		return this._nameMap[id];
	}

	usernameForUser(id: number): String {
		return this._usernameMap[id];
	}
}
