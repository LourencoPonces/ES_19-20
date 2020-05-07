import ClarificationRequest from './ClarificationRequest';
import User from '@/models/user/User';

export default class ClarificationRequestList {
	private _requests: ClarificationRequest[] = [];
	private _nameMap: {[username: string]: string} = {};

	constructor(jsonObj: any) {
		if (jsonObj) {
			this._requests = jsonObj.requests;
			this._nameMap = jsonObj.names;

			if (!this._requests || !this._nameMap)
				throw new Error('invalid ClarificationRequestList response from server');
		}
	}

	get requests(): ClarificationRequest[] {
		return this._requests;
	}

	nameForUser(username: string): string {
		return this._nameMap[username];
	}

	push(req: ClarificationRequest, user: User): void {
		this._requests.push(req);

		if (!this._nameMap[req.getCreatorUsername()]) {
			if (req.getCreatorUsername() != user.username)
				throw new Error('no data available for ClarificationRequest creator');

			this._nameMap[user.username] = user.name;
		}
	}
}
